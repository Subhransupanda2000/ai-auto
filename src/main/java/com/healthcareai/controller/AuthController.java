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

import com.healthcareai.dto.LoginRequest;
import com.healthcareai.dto.LoginResponse;
import com.healthcareai.dto.RegisterRequest;
import com.healthcareai.dto.UserResponse;
import com.healthcareai.entity.User;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.security.JwtService;
import com.healthcareai.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Staff authentication: {@code POST /api/auth/login} and
 * {@code POST /api/auth/register}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Staff authentication (ADMIN, DOCTOR, RECEPTIONIST)")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email/password and receive a JWT access token.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        User user = userService.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        String token = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(LoginResponse.bearer(token, jwtService.getAccessTokenTtlSeconds()));
    }

    /**
     * Registers a new staff user. The very first user in the system may
     * self-register (and is always created as ADMIN, regardless of the
     * requested role) to bootstrap the system; every subsequent
     * registration requires an authenticated ADMIN caller.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new staff user (bootstraps as ADMIN if no users exist yet; otherwise requires an ADMIN caller).")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request, Authentication authentication) {
        boolean isBootstrap = userService.count() == 0;
        if (!isBootstrap && !isAdmin(authentication)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only an administrator can register new staff users.");
        }

        com.healthcareai.entity.Role role = isBootstrap ? com.healthcareai.entity.Role.ADMIN : request.role();
        User user = userService.createUser(request.email(), request.password(), request.fullName(), role);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name()));
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
