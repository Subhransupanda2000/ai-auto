package com.healthcareai.seed;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.config.ClinicProperties;
import com.healthcareai.config.SeedProperties;
import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.AppointmentStatus;
import com.healthcareai.entity.Doctor;
import com.healthcareai.entity.FaqCategory;
import com.healthcareai.entity.FaqDocument;
import com.healthcareai.entity.Patient;
import com.healthcareai.entity.Tenant;
import com.healthcareai.repository.AppointmentRepository;
import com.healthcareai.repository.DoctorRepository;
import com.healthcareai.repository.FaqDocumentRepository;
import com.healthcareai.repository.PatientRepository;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.service.TenantService;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Populates the database with realistic demo data (doctors, patients,
 * appointments, and knowledge base/FAQ articles) on application startup, so
 * the app is immediately ready for a demo without any manual setup.
 *
 * <p>Each entity type is seeded independently and only when its table is
 * currently empty, so this runner is idempotent and safe to leave enabled
 * across restarts: it never inserts duplicate data, and pre-existing data
 * (e.g. real production records, or a partially-seeded database) is left
 * untouched.
 *
 * <p>Disable via {@code app.seed.enabled=false} (env {@code SEED_DEMO_DATA=false}).
 * Counts are configurable via {@code app.seed.*} (see {@link SeedProperties}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements CommandLineRunner {

    private static final int MAX_SLOT_SEARCH_ATTEMPTS = 25;

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final FaqDocumentRepository faqDocumentRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final SeedProperties seedProperties;
    private final ClinicProperties clinicProperties;

    private final DemoDataGenerator generator = new DemoDataGenerator();

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Demo data seed check starting (enabled via app.seed.enabled)...");

        Tenant tenant = ensureDefaultTenantSeeded();

        // Every clinical entity created below (doctors/patients/appointments/
        // knowledge base) is explicitly stamped with this tenant's id (see
        // each ensure*Seeded() method), and every count()/findAll() check
        // below is explicitly scoped to it too via TenantContext - see
        // com.healthcareai.tenant.TenantContext.
        TenantContext.runAs(tenant.getId(), () -> {
            List<Doctor> doctors = ensureDoctorsSeeded();
            List<Patient> patients = ensurePatientsSeeded();
            ensureAppointmentsSeeded(doctors, patients);
            ensureKnowledgeBaseSeeded(doctors);

            UUID tenantId = tenant.getId();
            BigDecimal revenue = appointmentRepository.sumConsultationFeeByTenantIdAndStatus(tenantId, AppointmentStatus.COMPLETED);
            long completedCount = appointmentRepository.findByTenantIdAndStatus(tenantId, AppointmentStatus.COMPLETED).size();
            log.info("Demo data seed check complete for tenant '{}'. Totals — doctors: {}, patients: {}, "
                            + "appointments: {}, knowledge base documents: {}. Revenue from {} completed appointments: ₹{}",
                    tenant.getSlug(), doctorRepository.countByTenantId(tenantId), patientRepository.countByTenantId(tenantId),
                    appointmentRepository.countByTenantId(tenantId), faqDocumentRepository.countByTenantId(tenantId),
                    completedCount, revenue);
        });
    }

    // ----------------------------------------------------------------- tenant

    private Tenant ensureDefaultTenantSeeded() {
        return tenantRepository.findBySlugIgnoreCase(seedProperties.defaultTenantSlug())
                .orElseGet(() -> {
                    String adminEmail = seedProperties.defaultAdminEmail();
                    String adminPassword = seedProperties.defaultAdminPassword();
                    boolean generatedCredentials = isBlank(adminEmail) || isBlank(adminPassword);
                    if (generatedCredentials) {
                        adminEmail = isBlank(adminEmail)
                                ? "admin@" + seedProperties.defaultTenantSlug() + ".local"
                                : adminEmail;
                        adminPassword = isBlank(adminPassword)
                                ? java.util.UUID.randomUUID().toString()
                                : adminPassword;
                    }

                    Tenant tenant = tenantService.createTenantWithAdmin(
                            seedProperties.defaultTenantName(), seedProperties.defaultTenantSlug(),
                            seedProperties.defaultAdminFullName(), adminEmail, adminPassword);

                    if (generatedCredentials) {
                        log.warn("Seeded default tenant '{}' with a generated admin login - email: {}, password: {}. "
                                        + "Set SEED_ADMIN_EMAIL/SEED_ADMIN_PASSWORD to control this instead.",
                                tenant.getSlug(), adminEmail, adminPassword);
                    } else {
                        log.info("Seeded default tenant '{}' with admin {}.", tenant.getSlug(), adminEmail);
                    }
                    return tenant;
                });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ---------------------------------------------------------------- doctors

    private List<Doctor> ensureDoctorsSeeded() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (doctorRepository.countByTenantId(tenantId) > 0) {
            log.info("Doctors table already has data; skipping doctor seeding.");
            return doctorRepository.findAllByTenantId(tenantId);
        }

        Set<String> usedEmails = new HashSet<>();
        Set<String> usedPhones = new HashSet<>();
        List<Doctor> doctors = new ArrayList<>();

        for (String specialty : DemoDataGenerator.SPECIALTIES) {
            String firstName = generator.firstName();
            String lastName = generator.lastName();
            DemoDataGenerator.WorkingSchedule schedule = generator.randomWorkingSchedule();
            int yearsExperience = 3 + ThreadLocalRandom.current().nextInt(28);

            Doctor doctor = Doctor.builder()
                    .tenantId(tenantId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .specialty(specialty)
                    .email(generator.uniqueEmail(firstName, lastName, usedEmails))
                    .phoneNumber(generator.uniquePhoneNumber(usedPhones))
                    .bio(generator.doctorBio(firstName, lastName, specialty, yearsExperience))
                    .active(true)
                    .workingHoursStart(schedule.start())
                    .workingHoursEnd(schedule.end())
                    .workingDays(schedule.days())
                    .build();
            doctors.add(doctor);
        }

        List<Doctor> saved = doctorRepository.saveAll(doctors);
        log.info("Seeded {} doctors.", saved.size());
        return saved;
    }

    // --------------------------------------------------------------- patients

    private List<Patient> ensurePatientsSeeded() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (patientRepository.countByTenantId(tenantId) > 0) {
            log.info("Patients table already has data; skipping patient seeding.");
            return patientRepository.findAllByTenantId(tenantId);
        }

        Set<String> usedPhones = new HashSet<>();
        Set<String> usedEmails = new HashSet<>();
        List<Patient> patients = new ArrayList<>();

        for (int i = 0; i < seedProperties.patientCount(); i++) {
            String firstName = generator.firstName();
            String lastName = generator.lastName();
            boolean hasEmail = ThreadLocalRandom.current().nextInt(100) < 85;

            Patient patient = Patient.builder()
                    .tenantId(tenantId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(hasEmail ? generator.uniqueEmail(firstName, lastName, usedEmails) : null)
                    .phoneNumber(generator.uniquePhoneNumber(usedPhones))
                    .dateOfBirth(generator.randomDateOfBirth())
                    .gender(generator.randomGender())
                    .address(generator.indianAddress())
                    .build();
            patients.add(patient);
        }

        List<Patient> saved = patientRepository.saveAll(patients);
        log.info("Seeded {} patients.", saved.size());
        return saved;
    }

    // ----------------------------------------------------------- appointments

    private void ensureAppointmentsSeeded(List<Doctor> doctors, List<Patient> patients) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (appointmentRepository.countByTenantId(tenantId) > 0) {
            log.info("Appointments table already has data; skipping appointment seeding.");
            return;
        }
        if (doctors.isEmpty() || patients.isEmpty()) {
            log.warn("Cannot seed appointments: no doctors or patients available.");
            return;
        }

        ZoneId zone = resolveZone();
        Instant now = Instant.now();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<String> usedSlots = new HashSet<>();
        List<Appointment> appointments = new ArrayList<>(seedProperties.appointmentCount());

        int target = seedProperties.appointmentCount();
        int created = 0;
        int guardCounter = 0;
        // Generous overall guard so a run of bad luck finding free slots can't loop forever.
        int maxIterations = target * (MAX_SLOT_SEARCH_ATTEMPTS + 1);

        while (created < target && guardCounter < maxIterations) {
            guardCounter++;
            Doctor doctor = doctors.get(random.nextInt(doctors.size()));
            Patient patient = patients.get(random.nextInt(patients.size()));

            ZonedDateTime slotStart = findAvailableSlot(doctor, zone, random, usedSlots);
            if (slotStart == null) {
                continue;
            }

            Instant startInstant = slotStart.toInstant();
            Instant endInstant = slotStart.plusMinutes(clinicProperties.appointmentSlotMinutes()).toInstant();
            AppointmentStatus status = randomStatusFor(startInstant, now, random);

            Appointment appointment = Appointment.builder()
                    .tenantId(tenantId)
                    .patient(patient)
                    .doctor(doctor)
                    .scheduledStart(startInstant)
                    .scheduledEnd(endInstant)
                    .status(status)
                    .reason(generator.visitReasonFor(doctor.getSpecialty()))
                    .consultationFee(generator.consultationFeeFor(doctor.getSpecialty()))
                    .build();
            appointments.add(appointment);
            created++;
        }

        if (created < target) {
            log.warn("Only able to generate {} of {} requested appointments without slot collisions.", created, target);
        }

        appointmentRepository.saveAll(appointments);
        log.info("Seeded {} appointments.", appointments.size());
    }

    /** Finds a free (doctor, start-time) slot within the doctor's working hours across the -30..+30 day window. */
    private ZonedDateTime findAvailableSlot(Doctor doctor, ZoneId zone, ThreadLocalRandom random, Set<String> usedSlots) {
        Set<DayOfWeek> workingDays = parseWorkingDays(doctor.getWorkingDays());
        int slotMinutes = clinicProperties.appointmentSlotMinutes();
        long totalSlotsInDay = java.time.Duration.between(doctor.getWorkingHoursStart(), doctor.getWorkingHoursEnd()).toMinutes() / slotMinutes;
        if (totalSlotsInDay <= 0) {
            return null;
        }

        for (int attempt = 0; attempt < MAX_SLOT_SEARCH_ATTEMPTS; attempt++) {
            int dayOffset = random.nextInt(-30, 31); // inclusive of -30, exclusive of 31 -> [-30, 30]
            LocalDate date = LocalDate.now(zone).plusDays(dayOffset);
            if (!workingDays.contains(date.getDayOfWeek())) {
                continue;
            }

            int slotIndex = random.nextInt((int) totalSlotsInDay);
            LocalTime slotTime = doctor.getWorkingHoursStart().plusMinutes((long) slotIndex * slotMinutes);
            ZonedDateTime candidate = ZonedDateTime.of(date, slotTime, zone);

            String key = doctor.getId() + "|" + candidate.toInstant();
            if (usedSlots.add(key)) {
                return candidate;
            }
        }
        return null;
    }

    private AppointmentStatus randomStatusFor(Instant scheduledStart, Instant now, ThreadLocalRandom random) {
        int roll = random.nextInt(100);
        if (scheduledStart.isBefore(now)) {
            if (roll < 75) return AppointmentStatus.COMPLETED;
            if (roll < 90) return AppointmentStatus.CANCELLED;
            return AppointmentStatus.RESCHEDULED;
        } else {
            if (roll < 60) return AppointmentStatus.SCHEDULED; // "BOOKED"
            if (roll < 75) return AppointmentStatus.RESCHEDULED;
            return AppointmentStatus.CANCELLED;
        }
    }

    private Set<DayOfWeek> parseWorkingDays(String workingDays) {
        Set<DayOfWeek> days = new HashSet<>();
        for (String code : workingDays.split(",")) {
            days.add(switch (code.trim().toUpperCase(Locale.ROOT)) {
                case "MON" -> DayOfWeek.MONDAY;
                case "TUE" -> DayOfWeek.TUESDAY;
                case "WED" -> DayOfWeek.WEDNESDAY;
                case "THU" -> DayOfWeek.THURSDAY;
                case "FRI" -> DayOfWeek.FRIDAY;
                case "SAT" -> DayOfWeek.SATURDAY;
                case "SUN" -> DayOfWeek.SUNDAY;
                default -> throw new IllegalArgumentException("Unknown working day code: " + code);
            });
        }
        return days;
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(clinicProperties.timezone());
        } catch (Exception e) {
            log.warn("Invalid clinic timezone '{}', falling back to UTC.", clinicProperties.timezone());
            return ZoneId.of("UTC");
        }
    }

    // ------------------------------------------------------------ knowledge base

    private void ensureKnowledgeBaseSeeded(List<Doctor> doctors) {
        if (faqDocumentRepository.countByTenantId(TenantContext.getCurrentTenantId()) > 0) {
            log.info("Knowledge base table already has data; skipping knowledge base/FAQ seeding.");
            return;
        }

        List<FaqDocument> documents = new ArrayList<>();
        documents.addAll(buildKnowledgeBaseArticles(doctors, seedProperties.knowledgeBaseArticleCount()));
        documents.addAll(buildFaqRecords(seedProperties.faqCount()));

        faqDocumentRepository.saveAll(documents);
        log.info("Seeded {} knowledge base articles and {} FAQ records ({} total documents). "
                        + "Embeddings are left null; run the knowledge base ingestion API to backfill them if semantic search is needed.",
                seedProperties.knowledgeBaseArticleCount(), seedProperties.faqCount(), documents.size());
    }

    private List<FaqDocument> buildKnowledgeBaseArticles(List<Doctor> doctors, int totalCount) {
        List<FaqDocument> articles = new ArrayList<>(totalCount);

        int doctorProfileCount = Math.min(doctors.size(), totalCount);
        for (int i = 0; i < doctorProfileCount; i++) {
            Doctor doctor = doctors.get(i);
            String workingHours = "%s to %s".formatted(doctor.getWorkingHoursStart(), doctor.getWorkingHoursEnd());
            DemoDataGenerator.KnowledgeArticle article = generator.doctorProfileArticle(
                    doctor.getFirstName(), doctor.getLastName(), doctor.getSpecialty(), workingHours, doctor.getWorkingDays());
            articles.add(toFaqDocument(FaqCategory.DOCTOR_PROFILE, article));
        }

        int remaining = totalCount - doctorProfileCount;
        int perCategory = remaining / 3;
        int leftover = remaining - (perCategory * 3);

        for (int i = 0; i < perCategory + (leftover > 0 ? 1 : 0); i++) {
            articles.add(toFaqDocument(FaqCategory.CLINIC_TIMINGS, generator.clinicTimingsArticle(i)));
        }
        for (int i = 0; i < perCategory + (leftover > 1 ? 1 : 0); i++) {
            articles.add(toFaqDocument(FaqCategory.INSURANCE, generator.insuranceArticle(i)));
        }
        for (int i = 0; i < perCategory; i++) {
            articles.add(toFaqDocument(FaqCategory.MEDICAL_SERVICE, generator.medicalServiceArticle(i)));
        }

        return articles;
    }

    private List<FaqDocument> buildFaqRecords(int count) {
        List<FaqDocument> faqs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            faqs.add(toFaqDocument(FaqCategory.FAQ, generator.faqRecord(i)));
        }
        return faqs;
    }

    private FaqDocument toFaqDocument(FaqCategory category, DemoDataGenerator.KnowledgeArticle article) {
        return FaqDocument.builder()
                .tenantId(TenantContext.getCurrentTenantId())
                .category(category)
                .title(article.title())
                .content(article.content())
                .source("demo-seed")
                .active(true)
                .build();
    }
}
