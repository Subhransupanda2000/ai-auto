package com.healthcareai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.config.FrontendProperties;
import com.healthcareai.entity.PasswordResetToken;
import com.healthcareai.entity.User;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.repository.PasswordResetTokenRepository;
import com.healthcareai.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final long TOKEN_TTL_MINUTES = 30;
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final FrontendProperties frontendProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            // Deliberately silent: don't reveal whether this email is registered.
            log.debug("Password reset requested for unknown email {}", email);
            return;
        }
        User user = userOpt.get();

        String rawToken = generateToken();
        passwordResetTokenRepository.invalidateAllForUser(user.getId());
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES))
                .build());

        String resetLink = frontendProperties.baseUrl() + "/reset-password?token=" + rawToken;
        String body = ("Hi %s,\n\nWe received a request to reset your password. Click the link below to "
                + "choose a new one. This link expires in %d minutes and can only be used once.\n\n%s\n\n"
                + "If you didn't request this, you can safely ignore this email.")
                .formatted(user.getFullName(), TOKEN_TTL_MINUTES, resetLink);
        notificationService.sendEmail(user.getEmail(), "Reset your password", body);
        log.info("Issued password reset token for user {}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hash(token))
                .filter(t -> t.isUsable(Instant.now()))
                .orElseThrow(() -> new BusinessRuleViolationException("This reset link is invalid or has expired."));

        userService.setPassword(resetToken.getUserId(), newPassword);
        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset completed for user {}", resetToken.getUserId());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available.", e);
        }
    }
}
