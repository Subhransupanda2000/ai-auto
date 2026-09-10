package com.healthcareai.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.healthcareai.config.JwtProperties;
import com.healthcareai.entity.Tenant;
import com.healthcareai.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Issues and validates JWT access tokens for staff (admin/doctor/receptionist)
 * authentication. Every token carries {@code tenantId} and {@code
 * tenantName} claims so that {@code JwtAuthenticationFilter} can populate
 * {@code TenantContext} for the duration of the request, and the frontend
 * can render the caller's own clinic name, without an extra database
 * round-trip.
 */
@Component
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String TENANT_ID_CLAIM = "tenantId";
    private static final String TENANT_NAME_CLAIM = "tenantName";

    private final JwtProperties properties;

    public String generateAccessToken(User user, Tenant tenant) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(ROLE_CLAIM, user.getRole().name())
                .claim(TENANT_ID_CLAIM, user.getTenantId().toString())
                .claim(TENANT_NAME_CLAIM, tenant.getName())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return properties.accessTokenTtlMinutes() * 60;
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid or expired token.", e);
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractTenantId(String token) {
        String tenantId = parseClaims(token).get(TENANT_ID_CLAIM, String.class);
        return tenantId != null ? UUID.fromString(tenantId) : null;
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
