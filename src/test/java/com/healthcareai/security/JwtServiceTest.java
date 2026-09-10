package com.healthcareai.security;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.healthcareai.config.JwtProperties;
import com.healthcareai.entity.Role;
import com.healthcareai.entity.Tenant;
import com.healthcareai.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtProperties properties = new JwtProperties(
            "unit-test-secret-key-that-is-long-enough-for-hs256-signing", 60, 7, "healthcareai-test");
    private final JwtService jwtService = new JwtService(properties);

    @Test
    void generateAccessToken_producesTokenWithSubjectAndRoleClaim() {
        UUID tenantId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).tenantId(tenantId).email("admin@example.com")
                .passwordHash("hash").fullName("Admin User").role(Role.ADMIN).build();
        Tenant tenant = Tenant.builder().id(tenantId).name("Sunrise Clinic").slug("sunrise-clinic").build();

        String token = jwtService.generateAccessToken(user, tenant);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@example.com");
        assertThat(jwtService.extractTenantId(token)).isEqualTo(tenantId);

        Claims claims = jwtService.parseClaims(token);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("tenantName", String.class)).isEqualTo("Sunrise Clinic");
        assertThat(claims.getIssuer()).isEqualTo("healthcareai-test");
    }

    @Test
    void parseClaims_throwsJwtException_forTamperedToken() {
        UUID tenantId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).tenantId(tenantId).email("user@example.com")
                .passwordHash("hash").fullName("User").role(Role.RECEPTIONIST).build();
        Tenant tenant = Tenant.builder().id(tenantId).name("Sunrise Clinic").slug("sunrise-clinic").build();
        String token = jwtService.generateAccessToken(user, tenant) + "tampered";

        assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(JwtException.class);
    }
}
