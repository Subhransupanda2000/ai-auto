package com.healthcareai.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.config.SuperAdminBootstrapProperties;
import com.healthcareai.entity.SuperAdmin;
import com.healthcareai.repository.SuperAdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates the very first platform super admin on startup, if (and only if)
 * none exists yet and {@code SUPER_ADMIN_EMAIL}/{@code SUPER_ADMIN_PASSWORD}
 * are configured. Runs unconditionally (unlike {@link DemoDataSeeder},
 * which only runs when {@code app.seed.enabled=true}) since a real
 * deployment still needs a way to bootstrap its first super admin without
 * demo data. Idempotent: does nothing once at least one super admin row
 * exists, so it's safe to leave running across restarts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class SuperAdminBootstrap implements CommandLineRunner {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminBootstrapProperties properties;

    @Override
    @Transactional
    public void run(String... args) {
        if (superAdminRepository.count() > 0) {
            log.info("A super admin already exists; skipping super-admin bootstrap.");
            return;
        }
        if (isBlank(properties.email()) || isBlank(properties.password())) {
            log.warn("No super admin exists yet, and SUPER_ADMIN_EMAIL/SUPER_ADMIN_PASSWORD are not set. "
                    + "Set both and restart the application to bootstrap the first platform super admin "
                    + "(required to onboard tenants via POST /api/super-admin/tenants).");
            return;
        }

        SuperAdmin superAdmin = SuperAdmin.builder()
                .email(properties.email())
                .passwordHash(passwordEncoder.encode(properties.password()))
                .fullName(properties.fullName())
                .build();
        superAdminRepository.save(superAdmin);
        log.info("Bootstrapped initial super admin '{}'.", superAdmin.getEmail());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
