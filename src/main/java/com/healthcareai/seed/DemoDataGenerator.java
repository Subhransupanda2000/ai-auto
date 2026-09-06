package com.healthcareai.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import net.datafaker.Faker;

/**
 * Generates realistic, India-flavoured demo data (names, contact details,
 * addresses, schedules, and knowledge-base copy) using DataFaker. Kept
 * separate from {@link DemoDataSeeder} so the persistence/orchestration
 * logic isn't tangled up with random-data generation.
 */
final class DemoDataGenerator {

    /** Doctor specialties; exactly 15 so each seeded doctor gets a distinct one. */
    static final List<String> SPECIALTIES = List.of(
            "General Physician", "Cardiologist", "Dermatologist", "Pediatrician",
            "Orthopedic Surgeon", "Gynecologist", "ENT Specialist", "Neurologist",
            "Psychiatrist", "Dentist", "Ophthalmologist", "Endocrinologist",
            "Gastroenterologist", "Urologist", "Pulmonologist"
    );

    private static final Map<String, BigDecimal> BASE_CONSULTATION_FEE = Map.ofEntries(
            Map.entry("General Physician", new BigDecimal("400")),
            Map.entry("Cardiologist", new BigDecimal("1200")),
            Map.entry("Dermatologist", new BigDecimal("700")),
            Map.entry("Pediatrician", new BigDecimal("500")),
            Map.entry("Orthopedic Surgeon", new BigDecimal("900")),
            Map.entry("Gynecologist", new BigDecimal("800")),
            Map.entry("ENT Specialist", new BigDecimal("600")),
            Map.entry("Neurologist", new BigDecimal("1500")),
            Map.entry("Psychiatrist", new BigDecimal("1000")),
            Map.entry("Dentist", new BigDecimal("500")),
            Map.entry("Ophthalmologist", new BigDecimal("650")),
            Map.entry("Endocrinologist", new BigDecimal("900")),
            Map.entry("Gastroenterologist", new BigDecimal("1100")),
            Map.entry("Urologist", new BigDecimal("950")),
            Map.entry("Pulmonologist", new BigDecimal("850"))
    );

    private static final Map<String, List<String>> VISIT_REASONS_BY_SPECIALTY = Map.ofEntries(
            Map.entry("General Physician", List.of("Fever and body ache", "Routine health checkup", "Cold and cough", "Annual physical exam", "Fatigue and weakness")),
            Map.entry("Cardiologist", List.of("Chest pain evaluation", "Routine cardiac checkup", "High blood pressure follow-up", "Palpitations", "Post-angioplasty review")),
            Map.entry("Dermatologist", List.of("Skin allergy", "Acne treatment", "Hair fall consultation", "Eczema follow-up", "Mole examination")),
            Map.entry("Pediatrician", List.of("Vaccination", "Growth and development checkup", "Fever in child", "Common cold", "Nutrition consultation")),
            Map.entry("Orthopedic Surgeon", List.of("Knee pain", "Back pain evaluation", "Post-fracture follow-up", "Joint stiffness", "Sports injury")),
            Map.entry("Gynecologist", List.of("Routine prenatal checkup", "Menstrual irregularity", "Postnatal checkup", "Annual gynecological exam", "PCOS consultation")),
            Map.entry("ENT Specialist", List.of("Ear pain", "Sinus congestion", "Hearing loss evaluation", "Sore throat", "Tonsillitis follow-up")),
            Map.entry("Neurologist", List.of("Chronic headache", "Migraine follow-up", "Dizziness evaluation", "Numbness in limbs", "Memory concerns")),
            Map.entry("Psychiatrist", List.of("Anxiety consultation", "Sleep disorder", "Stress management", "Follow-up medication review", "Mood evaluation")),
            Map.entry("Dentist", List.of("Tooth pain", "Routine dental cleaning", "Cavity filling", "Root canal follow-up", "Braces consultation")),
            Map.entry("Ophthalmologist", List.of("Blurred vision", "Routine eye checkup", "Eye redness", "Cataract evaluation", "Glasses prescription renewal")),
            Map.entry("Endocrinologist", List.of("Diabetes follow-up", "Thyroid evaluation", "Weight management", "Hormonal imbalance", "Routine sugar checkup")),
            Map.entry("Gastroenterologist", List.of("Acidity and reflux", "Abdominal pain", "Digestive issues", "Follow-up endoscopy review", "Liver function checkup")),
            Map.entry("Urologist", List.of("Urinary tract infection", "Kidney stone follow-up", "Routine prostate checkup", "Bladder concerns", "Post-surgery review")),
            Map.entry("Pulmonologist", List.of("Persistent cough", "Asthma follow-up", "Breathlessness evaluation", "Allergy-related wheezing", "Routine lung function test"))
    );

    private static final List<String> WEEKDAY_ONLY = List.of("MON", "TUE", "WED", "THU", "FRI");
    private static final List<String> WEEKDAY_PLUS_SATURDAY = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT");

    private static final String[] EMAIL_DOMAINS = {"gmail.com", "yahoo.com", "outlook.com", "rediffmail.com"};

    private final Faker faker = new Faker(Locale.of("en", "IN"));
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    String firstName() {
        return faker.name().firstName();
    }

    String lastName() {
        return faker.name().lastName();
    }

    /** Builds a unique email of the form {@code first.last@domain}, retrying with a numeric suffix on collision. */
    String uniqueEmail(String firstName, String lastName, Set<String> used) {
        String base = (firstName + "." + lastName).toLowerCase(Locale.ROOT).replaceAll("[^a-z.]", "");
        String domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        String candidate = base + "@" + domain;
        int suffix = 1;
        while (!used.add(candidate)) {
            candidate = base + suffix + "@" + domain;
            suffix++;
        }
        return candidate;
    }

    /** Builds a unique 10-digit Indian mobile number in {@code +91XXXXXXXXXX} format. */
    String uniquePhoneNumber(Set<String> used) {
        String candidate;
        do {
            int firstDigit = 6 + random.nextInt(4); // 6, 7, 8, or 9
            StringBuilder sb = new StringBuilder("+91").append(firstDigit);
            for (int i = 0; i < 9; i++) {
                sb.append(random.nextInt(10));
            }
            candidate = sb.toString();
        } while (!used.add(candidate));
        return candidate;
    }

    String indianAddress() {
        String street = faker.address().streetAddress();
        String city = faker.address().city();
        String state = faker.address().state();
        String pincode = String.valueOf(100000 + random.nextInt(900000));
        return "%s, %s, %s - %s".formatted(street, city, state, pincode);
    }

    String randomGender() {
        int roll = random.nextInt(100);
        if (roll < 49) return "MALE";
        if (roll < 98) return "FEMALE";
        return "OTHER";
    }

    LocalDate randomDateOfBirth() {
        int age = 1 + random.nextInt(90);
        LocalDate today = LocalDate.now();
        LocalDate birthYear = today.minusYears(age);
        return birthYear.minusDays(random.nextInt(365));
    }

    record WorkingSchedule(LocalTime start, LocalTime end, String days) {
    }

    WorkingSchedule randomWorkingSchedule() {
        int[] startHours = {8, 9, 9, 10};
        LocalTime start = LocalTime.of(startHours[random.nextInt(startHours.length)], random.nextBoolean() ? 30 : 0);
        int shiftHours = 7 + random.nextInt(3); // 7, 8, or 9 hour clinic day
        LocalTime end = start.plusHours(shiftHours);
        List<String> pattern = random.nextInt(100) < 70 ? WEEKDAY_ONLY : WEEKDAY_PLUS_SATURDAY;
        return new WorkingSchedule(start, end, String.join(",", pattern));
    }

    BigDecimal consultationFeeFor(String specialty) {
        BigDecimal base = BASE_CONSULTATION_FEE.getOrDefault(specialty, new BigDecimal("500"));
        double variance = 0.85 + random.nextDouble() * 0.3; // +/-15%
        return base.multiply(BigDecimal.valueOf(variance)).setScale(2, RoundingMode.HALF_UP);
    }

    String visitReasonFor(String specialty) {
        List<String> reasons = VISIT_REASONS_BY_SPECIALTY.getOrDefault(specialty, VISIT_REASONS_BY_SPECIALTY.get("General Physician"));
        return reasons.get(random.nextInt(reasons.size()));
    }

    String doctorBio(String firstName, String lastName, String specialty, int yearsExperience) {
        return "Dr. %s %s is a %s with %d years of experience, dedicated to providing compassionate, evidence-based care to every patient."
                .formatted(firstName, lastName, specialty, yearsExperience);
    }

    Faker faker() {
        return faker;
    }

    ThreadLocalRandom random() {
        return random;
    }

    private static final List<String> CLINIC_SERVICES = List.of(
            "General Consultation", "Vaccination", "Health Checkup Package", "Diagnostic Lab Tests",
            "Physiotherapy", "Dental Cleaning", "Minor Surgery", "Teleconsultation", "Maternity Care",
            "Emergency Care", "Nutrition Counselling", "Eye Screening", "ECG & Cardiac Screening",
            "Skin Care Treatment", "Chronic Disease Management"
    );

    private static final List<String> INSURANCE_PROVIDERS = List.of(
            "Star Health Insurance", "HDFC ERGO Health", "ICICI Lombard Health Care", "Niva Bupa Health Insurance",
            "Care Health Insurance", "Aditya Birla Health Insurance", "Tata AIG Health", "Bajaj Allianz Health",
            "National Insurance", "New India Assurance"
    );

    /** Curated, realistic clinic FAQ question/answer pairs used before falling back to generated ones. */
    private static final List<String[]> CURATED_FAQS = List.of(
            new String[]{"What are your clinic's operating hours?", "The clinic is open Monday to Saturday from 9:00 AM to 5:00 PM. We are closed on Sundays and public holidays."},
            new String[]{"Do I need an appointment to see a doctor?", "Walk-ins are welcome, but booking an appointment in advance through our AI receptionist or front desk helps reduce your waiting time."},
            new String[]{"How can I book, reschedule, or cancel an appointment?", "You can book, reschedule, or cancel an appointment by chatting with our AI receptionist on WhatsApp, SMS, or the website, or by calling the front desk."},
            new String[]{"Which insurance providers do you accept?", "We accept most major health insurance providers. Please share your policy details with our front desk to confirm coverage before your visit."},
            new String[]{"Do you offer teleconsultation?", "Yes, we offer video and phone teleconsultations for a range of specialties. Ask our AI receptionist to book a virtual visit."},
            new String[]{"What should I bring to my first appointment?", "Please bring a valid photo ID, any previous medical records, current medications, and your insurance card if applicable."},
            new String[]{"How do I get my lab test results?", "Lab results are typically available within 24-48 hours and can be accessed through our patient portal or by contacting the front desk."},
            new String[]{"Is parking available at the clinic?", "Yes, free parking is available for patients and visitors in the clinic's dedicated parking area."},
            new String[]{"Do you treat pediatric patients?", "Yes, our pediatricians see patients from infancy through adolescence for routine checkups, vaccinations, and illness."},
            new String[]{"What is your cancellation policy?", "We request at least 4 hours' notice for cancellations so the slot can be offered to another patient. Late cancellations may incur a nominal fee."},
            new String[]{"Do you provide home sample collection for lab tests?", "Yes, we offer home sample collection for select diagnostic tests. Please call the front desk to schedule a pickup."},
            new String[]{"How do I request a prescription refill?", "You can request a prescription refill through the AI receptionist or by contacting your doctor's office directly."},
            new String[]{"Are emergency services available?", "We handle urgent, non-life-threatening cases during clinic hours. For medical emergencies, please call your local emergency number or visit the nearest hospital."},
            new String[]{"Can I switch doctors within the clinic?", "Yes, you're welcome to consult with any available doctor. Let the front desk know if you'd like to switch for future visits."},
            new String[]{"Do you offer health checkup packages?", "Yes, we offer a range of preventive health checkup packages for individuals, couples, and senior citizens."}
    );

    private static final List<String> FAQ_FALLBACK_TOPICS = List.of(
            "billing and payment options", "online appointment booking", "doctor availability on weekends",
            "COVID-19 safety precautions", "vaccination schedules for children", "senior citizen discounts",
            "referral to specialists", "reports and record access", "waiting time expectations",
            "language support for consultations", "wheelchair accessibility", "pharmacy on premises"
    );

    record KnowledgeArticle(String title, String content) {
    }

    KnowledgeArticle doctorProfileArticle(String firstName, String lastName, String specialty, String workingHours, String workingDays) {
        String title = "Dr. %s %s — %s".formatted(firstName, lastName, specialty);
        String content = "Dr. %s %s is one of our experienced %s specialists, available %s during working hours of %s. %s"
                .formatted(firstName, lastName, specialty, workingDays, workingHours, faker.lorem().sentence(12));
        return new KnowledgeArticle(title, content);
    }

    KnowledgeArticle clinicTimingsArticle(int index) {
        String title = "Clinic Timings & Availability #%d".formatted(index);
        String content = "Our clinic operates Monday through Saturday, with extended hours for select departments. "
                + faker.lorem().sentence(15) + " Please contact the front desk or the AI receptionist to confirm the latest schedule for a specific department.";
        return new KnowledgeArticle(title, content);
    }

    KnowledgeArticle insuranceArticle(int index) {
        String provider = INSURANCE_PROVIDERS.get(index % INSURANCE_PROVIDERS.size());
        String title = "Insurance Coverage: %s".formatted(provider);
        String content = "We are an empanelled provider for %s. Cashless treatment is available for eligible plans, subject to pre-authorization. %s"
                .formatted(provider, faker.lorem().sentence(14));
        return new KnowledgeArticle(title, content);
    }

    KnowledgeArticle medicalServiceArticle(int index) {
        String service = CLINIC_SERVICES.get(index % CLINIC_SERVICES.size());
        String title = "Service Spotlight: %s".formatted(service);
        String content = "Our %s service is designed to provide timely, high-quality care. %s Please ask our AI receptionist or front desk for pricing and availability."
                .formatted(service, faker.lorem().sentence(16));
        return new KnowledgeArticle(title, content);
    }

    /** Returns a question/answer pair for FAQ record number {@code index} (0-based), reusing curated FAQs first. */
    KnowledgeArticle faqRecord(int index) {
        if (index < CURATED_FAQS.size()) {
            String[] qa = CURATED_FAQS.get(index);
            return new KnowledgeArticle(qa[0], qa[1]);
        }
        String topic = FAQ_FALLBACK_TOPICS.get(index % FAQ_FALLBACK_TOPICS.size());
        String question = "What should I know about " + topic + "?";
        String answer = faker.lorem().sentence(18) + " For more details, please reach out to our front desk or ask the AI receptionist about " + topic + ".";
        return new KnowledgeArticle(question, answer);
    }
}
