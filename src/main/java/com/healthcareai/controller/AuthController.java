package com.healthcareai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.ChangePasswordRequest;
import com.healthcareai.dto.ForgotPasswordRequest;
import com.healthcareai.dto.LoginRequest;
import com.healthcareai.dto.LoginResponse;
import com.healthcareai.dto.MessageResponse;
import com.healthcareai.dto.RefreshTokenRequest;
import com.healthcareai.dto.RegisterRequest;
import com.healthcareai.dto.ResetPasswordRequest;
import com.healthcareai.dto.UserResponse;
import com.healthcareai.entity.Tenant;
import com.healthcareai.entity.User;
import com.healthcareai.security.JwtService;
import com.healthcareai.service.PasswordResetService;
import com.healthcareai.service.RefreshTokenService;
import com.healthcareai.service.TenantService;
import com.healthcareai.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Staff authentication: {@code POST /api/auth/login}, {@code
 * POST /api/auth/register}, and the forgot/reset/change password flows.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Staff authentication (ADMIN, DOCTOR, RECEPTIONIST)")
public class AuthController {

    private static final MessageResponse FORGOT_PASSWORD_RESPONSE =
            new MessageResponse("If an account exists for that email, a password reset link has been sent.");

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TenantService tenantService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email/password and receive a JWT access token plus a "
            + "long-lived refresh token.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        User user = userService.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        Tenant tenant = tenantService.findById(user.getTenantId())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));
        if (!tenant.isActive()) {
            throw new BadCredentialsException("This clinic's account has been deactivated. Contact support.");
        }

        String accessToken = jwtService.generateAccessToken(user, tenant);
        RefreshTokenService.IssuedToken refreshToken = refreshTokenService.issue(user.getId());
        return ResponseEntity.ok(LoginResponse.bearer(accessToken, refreshToken.rawToken(), jwtService.getAccessTokenTtlSeconds()));
    }

    /**
     * Exchanges a still-valid refresh token for a new access token (and, since
     * refresh tokens are single-use/rotated, a new refresh token too) without
     * requiring the user's password again. This is what lets a staff session
     * stay signed in across the access token's short TTL without a full
     * re-login every {@code app.jwt.access-token-ttl-minutes}.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access token (and a new refresh token).")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(request.refreshToken());

        User user = userService.findById(rotation.userId())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired session."));
        Tenant tenant = tenantService.findById(user.getTenantId())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired session."));
        if (!tenant.isActive() || !user.isEnabled()) {
            throw new BadCredentialsException("This account no longer has access. Contact your administrator.");
        }

        String accessToken = jwtService.generateAccessToken(user, tenant);
        return ResponseEntity.ok(LoginResponse.bearer(
                accessToken, rotation.newToken().rawToken(), jwtService.getAccessTokenTtlSeconds()));
    }

    /** Revokes a refresh token so it can no longer be used to silently renew
     * the session - called when the user explicitly signs out. Always
     * succeeds (even for an already-invalid token) since the end state
     * ("this token doesn't work") is the same either way. */
    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token (sign out of this session).")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Signed out."));
    }

    /**
     * Registers a new staff user within the caller's own clinic (tenant).
     * There is no self-service bootstrap path any more: every tenant's
     * first user (its ADMIN) is created exclusively by a super admin via
     * the tenant onboarding endpoint ({@code POST /api/super-admin/tenants}),
     * so every request here always requires an authenticated ADMIN caller,
     * and the new user always inherits the caller's tenant.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new staff user in the caller's clinic (requires an ADMIN caller).")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request, Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only an administrator can register new staff users.");
        }

        User caller = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired session."));
        User user = userService.createUser(request.email(), request.password(), request.fullName(), request.role(), caller.getTenantId());
        return ResponseEntity.ok(toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(),
                user.isEnabled(), user.getCreatedAt());
    }

    /**
     * Starts the forgot-password flow: emails a single-use reset link if
     * the address belongs to a staff account. Always returns the same
     * response either way, so this can't be used to enumerate accounts.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email.")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(FORGOT_PASSWORD_RESPONSE);
    }

    /** Completes the forgot-password flow using the token from the emailed link. */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset a password using a forgot-password token.")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Your password has been reset. You can now sign in."));
    }

    /** Self-service password change for an already-authenticated user. */
    @PostMapping("/change-password")
    @Operation(summary = "Change the caller's own password (requires the current password).")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            Authentication authentication) {
        userService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Your password has been updated."));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
