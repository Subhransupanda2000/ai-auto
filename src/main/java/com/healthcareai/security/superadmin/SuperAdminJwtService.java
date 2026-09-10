package com.healthcareai.security.superadmin;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.healthcareai.config.JwtProperties;
import com.healthcareai.entity.SuperAdmin;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Issues and validates JWTs for the super-admin platform login, completely
 * separate from staff ({@code User}) authentication in {@link
 * com.healthcareai.security.JwtService}. Tokens carry a distinct
 * {@code principalType} claim and issuer suffix so that a staff token can
 * never be mistaken for a super-admin one (or vice versa) even though both
 * happen to be signed with the same HMAC secret.
 */
@Component
@RequiredArgsConstructor
public class SuperAdminJwtService {

    private static final String PRINCIPAL_TYPE_CLAIM = "principalType";
    private static final String PRINCIPAL_TYPE_VALUE = "SUPER_ADMIN";

    private final JwtProperties properties;

    public String generateAccessToken(SuperAdmin superAdmin) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(superAdmin.getEmail())
                .claim(PRINCIPAL_TYPE_CLAIM, PRINCIPAL_TYPE_VALUE)
                .issuer(properties.issuer() + "-super-admin")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return properties.accessTokenTtlMinutes() * 60;
    }

    /** Parses and validates the token, additionally rejecting anything that
     * isn't a super-admin token (e.g. a staff {@code User} token). */
    public Claims parseSuperAdminClaims(String token) {
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
            throw new JwtException("Not a super-admin token.");
        }
        return claims;
    }

    public String extractEmail(String token) {
        return parseSuperAdminClaims(token).getSubject();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
