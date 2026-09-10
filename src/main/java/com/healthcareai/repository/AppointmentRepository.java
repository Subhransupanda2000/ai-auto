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

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.id = :id and a.tenantId = :tenantId")
    Optional<Appointment> findWithPatientAndDoctorByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
            select a from Appointment a join fetch a.patient join fetch a.doctor
            where a.patient.id = :patientId and a.tenantId = :tenantId
            order by a.scheduledStart desc
            """)
    List<Appointment> findByTenantIdAndPatientIdOrderByScheduledStartDesc(@Param("tenantId") UUID tenantId,
                                                                           @Param("patientId") UUID patientId);

    @Query("""
            select a from Appointment a join fetch a.patient join fetch a.doctor
            where a.doctor.id = :doctorId and a.tenantId = :tenantId
            order by a.scheduledStart desc
            """)
    List<Appointment> findByTenantIdAndDoctorIdOrderByScheduledStartDesc(@Param("tenantId") UUID tenantId,
                                                                          @Param("doctorId") UUID doctorId);

    List<Appointment> findByTenantIdAndStatus(UUID tenantId, AppointmentStatus status);

    long countByTenantId(UUID tenantId);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.tenantId = :tenantId order by a.scheduledStart desc")
    List<Appointment> findAllWithPatientAndDoctor(@Param("tenantId") UUID tenantId);

    @Query("""
            select a from Appointment a
            where a.tenantId = :tenantId
              and a.doctor.id = :doctorId
              and a.status <> :excludedStatus
              and a.scheduledStart < :end
              and a.scheduledEnd > :start
            """)
    List<Appointment> findOverlapping(@Param("tenantId") UUID tenantId,
                                       @Param("doctorId") UUID doctorId,
                                       @Param("start") Instant start,
                                       @Param("end") Instant end,
                                       @Param("excludedStatus") AppointmentStatus excludedStatus);

    @Query("""
            select a from Appointment a
            where a.tenantId = :tenantId
              and a.doctor.id = :doctorId
              and a.status <> :excludedStatus
              and a.scheduledStart >= :from
              and a.scheduledStart < :to
            order by a.scheduledStart asc
            """)
    List<Appointment> findByDoctorAndDateRange(@Param("tenantId") UUID tenantId,
                                                @Param("doctorId") UUID doctorId,
                                                @Param("from") Instant from,
                                                @Param("to") Instant to,
                                                @Param("excludedStatus") AppointmentStatus excludedStatus);

    /** Intentionally NOT tenant-scoped: the reminder scheduler is a background
     * job with no request/tenant context, and must sweep every tenant's
     * due appointments in one pass. */
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

    /** Total realized revenue: the sum of {@code consultationFee} across a tenant's appointments in the given status. */
    @Query("select coalesce(sum(a.consultationFee), 0) from Appointment a where a.tenantId = :tenantId and a.status = :status")
    BigDecimal sumConsultationFeeByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") AppointmentStatus status);

    /** Same as above, further restricted to appointments scheduled in
     * {@code [from, to)} - backs the dashboard's date-range revenue filter. */
    @Query("""
            select coalesce(sum(a.consultationFee), 0) from Appointment a
            where a.tenantId = :tenantId and a.status = :status
              and a.scheduledStart >= :from and a.scheduledStart < :to
            """)
    BigDecimal sumConsultationFeeByTenantIdAndStatusAndScheduledStartBetween(@Param("tenantId") UUID tenantId,
                                                                              @Param("status") AppointmentStatus status,
                                                                              @Param("from") Instant from,
                                                                              @Param("to") Instant to);

    long countByTenantIdAndStatusAndScheduledStartGreaterThanEqualAndScheduledStartLessThan(
            UUID tenantId, AppointmentStatus status, Instant from, Instant to);
}
