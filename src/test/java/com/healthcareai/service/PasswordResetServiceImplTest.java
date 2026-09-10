package com.healthcareai.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.healthcareai.config.FrontendProperties;
import com.healthcareai.entity.PasswordResetToken;
import com.healthcareai.entity.Role;
import com.healthcareai.entity.User;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.repository.PasswordResetTokenRepository;
import com.healthcareai.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;

    private final FrontendProperties frontendProperties = new FrontendProperties("http://localhost:5173");

    private PasswordResetServiceImpl passwordResetService;

    private User sampleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .email("admin@example.com")
                .passwordHash("hash")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void requestReset_forKnownEmail_issuesTokenAndSendsEmail() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository, passwordResetTokenRepository, userService, notificationService, frontendProperties);
        User user = sampleUser();
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.requestReset(user.getEmail());

        verify(passwordResetTokenRepository).invalidateAllForUser(user.getId());
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getTokenHash()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());

        verify(notificationService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void requestReset_forUnknownEmail_doesNothingSilently() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository, passwordResetTokenRepository, userService, notificationService, frontendProperties);
        when(userRepository.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestReset("nobody@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(notificationService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void resetPassword_withValidToken_updatesPasswordAndConsumesToken() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository, passwordResetTokenRepository, userService, notificationService, frontendProperties);

        // Capture the raw token issued by requestReset so we can present it back
        // to resetPassword, exactly like a real emailed link would.
        User user = sampleUser();
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        ArgumentCaptor<PasswordResetToken> savedTokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        when(passwordResetTokenRepository.save(savedTokenCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);

        passwordResetService.requestReset(user.getEmail());
        verify(notificationService).sendEmail(eq(user.getEmail()), anyString(), emailBodyCaptor.capture());

        String emailBody = emailBodyCaptor.getValue();
        String afterToken = emailBody.substring(emailBody.indexOf("token=") + "token=".length());
        String rawToken = afterToken.split("\\s", 2)[0];
        PasswordResetToken persistedToken = savedTokenCaptor.getValue();

        when(passwordResetTokenRepository.findByTokenHash(persistedToken.getTokenHash()))
                .thenReturn(Optional.of(persistedToken));

        passwordResetService.resetPassword(rawToken, "BrandNewPassword123");

        verify(userService).setPassword(user.getId(), "BrandNewPassword123");
        assertThat(persistedToken.getUsedAt()).isNotNull();
    }

    @Test
    void resetPassword_withUnknownToken_throwsBusinessRuleViolation() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository, passwordResetTokenRepository, userService, notificationService, frontendProperties);
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("not-a-real-token", "NewPassword123"))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(userService, never()).setPassword(any(), any());
    }

    @Test
    void resetPassword_withExpiredToken_throwsBusinessRuleViolation() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository, passwordResetTokenRepository, userService, notificationService, frontendProperties);
        PasswordResetToken expired = PasswordResetToken.builder()
                .userId(UUID.randomUUID())
                .tokenHash("some-hash")
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", "NewPassword123"))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(userService, never()).setPassword(any(), any());
    }

    @Test
    void resetPassword_withAlreadyUsedToken_throwsBusinessRuleViolation() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository, passwordResetTokenRepository, userService, notificationService, frontendProperties);
        PasswordResetToken used = PasswordResetToken.builder()
                .userId(UUID.randomUUID())
                .tokenHash("some-hash")
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .usedAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> passwordResetService.resetPassword("used-token", "NewPassword123"))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(userService, never()).setPassword(any(), any());
    }
}
