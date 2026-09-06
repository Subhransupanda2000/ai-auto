package com.healthcareai.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.id = :id")
    Optional<Appointment> findWithPatientAndDoctorById(@Param("id") UUID id);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.patient.id = :patientId order by a.scheduledStart desc")
    List<Appointment> findByPatientIdOrderByScheduledStartDesc(@Param("patientId") UUID patientId);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.doctor.id = :doctorId order by a.scheduledStart desc")
    List<Appointment> findByDoctorIdOrderByScheduledStartDesc(@Param("doctorId") UUID doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor order by a.scheduledStart desc")
    List<Appointment> findAllWithPatientAndDoctor();

    @Query("""
            select a from Appointment a
            where a.doctor.id = :doctorId
              and a.status <> :excludedStatus
              and a.scheduledStart < :end
              and a.scheduledEnd > :start
            """)
    List<Appointment> findOverlapping(@Param("doctorId") UUID doctorId,
                                       @Param("start") Instant start,
                                       @Param("end") Instant end,
                                       @Param("excludedStatus") AppointmentStatus excludedStatus);

    @Query("""
            select a from Appointment a
            where a.doctor.id = :doctorId
              and a.status <> :excludedStatus
              and a.scheduledStart >= :from
              and a.scheduledStart < :to
            order by a.scheduledStart asc
            """)
    List<Appointment> findByDoctorAndDateRange(@Param("doctorId") UUID doctorId,
                                                @Param("from") Instant from,
                                                @Param("to") Instant to,
                                                @Param("excludedStatus") AppointmentStatus excludedStatus);

    @Query("""
            select a from Appointment a
            join fetch a.patient
            join fetch a.doctor
            where a.status in :statuses
              and a.reminderSentAt is null
              and a.scheduledStart between :from and :to
            """)
    List<Appointment> findDueForReminder(@Param("from") Instant from,
                                          @Param("to") Instant to,
                                          @Param("statuses") Collection<AppointmentStatus> statuses);

    /** Total realized revenue: the sum of {@code consultationFee} across appointments in the given status. */
    @Query("select coalesce(sum(a.consultationFee), 0) from Appointment a where a.status = :status")
    BigDecimal sumConsultationFeeByStatus(@Param("status") AppointmentStatus status);
}
