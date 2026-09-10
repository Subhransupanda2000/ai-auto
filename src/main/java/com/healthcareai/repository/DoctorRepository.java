package com.healthcareai.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcareai.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findAllByTenantId(UUID tenantId);

    long countByTenantId(UUID tenantId);

    Optional<Doctor> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Doctor> findByTenantIdAndActiveTrue(UUID tenantId);

    List<Doctor> findByTenantIdAndSpecialtyIgnoreCaseAndActiveTrue(UUID tenantId, String specialty);

    Optional<Doctor> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    @Query("""
            select d from Doctor d
            where d.tenantId = :tenantId
              and d.active = true
              and (lower(d.specialty) like lower(concat('%', :query, '%'))
                   or lower(d.firstName) like lower(concat('%', :query, '%'))
                   or lower(d.lastName) like lower(concat('%', :query, '%')))
            """)
    List<Doctor> search(@Param("tenantId") UUID tenantId, @Param("query") String query);
}
