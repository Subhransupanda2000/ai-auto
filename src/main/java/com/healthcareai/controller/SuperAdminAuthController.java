package com.healthcareai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.LoginRequest;
import com.healthcareai.dto.LoginResponse;
import com.healthcareai.entity.SuperAdmin;
import com.healthcareai.repository.SuperAdminRepository;
import com.healthcareai.security.superadmin.SuperAdminJwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Platform-level super-admin authentication, entirely separate from staff
 * login ({@link AuthController}): {@code POST /api/super-admin/auth/login}.
 * There is no self-registration endpoint - super admin accounts are
 * provisioned out-of-band (e.g. a one-off bootstrap script/seed), since
 * they are the root of trust for creating tenants.
 */
@RestController
@RequestMapping("/api/super-admin/auth")
@RequiredArgsConstructor
@Tag(name = "Super Admin Auth", description = "Platform super-admin authentication")
public class SuperAdminAuthController {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminJwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate as a super admin and receive a JWT access token.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SuperAdmin superAdmin = superAdminRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), superAdmin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtService.generateAccessToken(superAdmin);
        // Super admins have no refresh-token flow (see class docs) - this
        // surface is intentionally simpler and lower-traffic than staff
        // login, so a hard re-login on token expiry is an acceptable
        // trade-off. LoginResponse.refreshToken is simply null here.
        return ResponseEntity.ok(LoginResponse.bearer(token, null, jwtService.getAccessTokenTtlSeconds()));
    }
}
