package com.healthcareai.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcareai.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revokes every still-usable refresh token for a user - called on
     * logout-all-devices and, defensively, whenever that user's password
     * changes for any reason (self-service, forgot-password, or an admin
     * resetting it for them), so old sessions can't outlive a password
     * that's no longer supposed to work. */
    @Modifying
    @Query("update RefreshToken t set t.revokedAt = CURRENT_TIMESTAMP where t.userId = :userId and t.revokedAt is null")
    void revokeAllForUser(@Param("userId") UUID userId);
}
