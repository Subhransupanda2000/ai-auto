package com.healthcareai.security.demo;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.healthcareai.config.DemoProperties;
import com.healthcareai.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Issues and validates JWTs for the public read-only demo preview,
 * completely separate from staff ({@code JwtService}) and super-admin
 * ({@code SuperAdminJwtService}) authentication - a demo token carries a
 * distinct {@code principalType} claim so it can never be mistaken for
 * either, even though all three happen to share the same HMAC secret.
 * There is no persisted account or password behind a demo token: it's
 * handed out directly by {@code EnquiryService} on signup and simply
 * expires, with no refresh.
 */
@Component
@RequiredArgsConstructor
public class DemoJwtService {

    private static final String PRINCIPAL_TYPE_CLAIM = "principalType";
    private static final String PRINCIPAL_TYPE_VALUE = "DEMO";
    private static final String TENANT_ID_CLAIM = "tenantId";
    private static final String TENANT_NAME_CLAIM = "tenantName";
    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final DemoProperties demoProperties;

    public String generateDemoAccessToken(String email, UUID tenantId, String tenantName) {
        Instant now = Instant.now();
        Instant expiry = now.plus(demoProperties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(email)
                .claim(PRINCIPAL_TYPE_CLAIM, PRINCIPAL_TYPE_VALUE)
                .claim(ROLE_CLAIM, "DEMO")
                .claim(TENANT_ID_CLAIM, tenantId.toString())
                .claim(TENANT_NAME_CLAIM, tenantName)
                .issuer(jwtProperties.issuer() + "-demo")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return demoProperties.accessTokenTtlMinutes() * 60;
    }

    /** Parses and validates the token, additionally rejecting anything
     * that isn't a demo token (e.g. a staff or super-admin token). */
    public Claims parseDemoClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid or expired token.", e);
        }
        if (!PRINCIPAL_TYPE_VALUE.equals(claims.get(PRINCIPAL_TYPE_CLAIM, String.class))) {
            throw new JwtException("Not a demo token.");
        }
        return claims;
    }

    public String extractEmail(String token) {
        return parseDemoClaims(token).getSubject();
    }

    public UUID extractTenantId(String token) {
        return UUID.fromString(parseDemoClaims(token).get(TENANT_ID_CLAIM, String.class));
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
