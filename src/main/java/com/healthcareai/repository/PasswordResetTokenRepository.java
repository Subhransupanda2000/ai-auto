package com.healthcareai.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcareai.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Invalidates any still-usable tokens for a user, e.g. before issuing a
     * new one, so only the most recently requested reset link works. */
    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = CURRENT_TIMESTAMP where t.userId = :userId and t.usedAt is null")
    void invalidateAllForUser(@Param("userId") UUID userId);
}
