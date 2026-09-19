package com.healthcareai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.healthcareai.security.JwtAuthenticationFilter;
import com.healthcareai.security.demo.DemoAuthenticationFilter;
import com.healthcareai.security.demo.DemoModeWriteRestrictionFilter;
import com.healthcareai.security.superadmin.SuperAdminAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Stateless JWT-based security configuration. Public documentation
 * endpoints are open; staff (patient/doctor/appointment/chat) endpoints
 * require authentication and are restricted by role via
 * {@code requestMatchers(...).hasAnyRole(...)}. Super-admin endpoints
 * ({@code /api/super-admin/**}, tenant onboarding) are a fully separate
 * concern secured by {@link SuperAdminAuthenticationFilter} and
 * {@code ROLE_SUPER_ADMIN} - a super admin has no tenant and never touches
 * clinic data, and a staff member's token is never accepted there.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SuperAdminAuthenticationFilter superAdminAuthenticationFilter;
    private final DemoAuthenticationFilter demoAuthenticationFilter;
    private final DemoModeWriteRestrictionFilter demoModeWriteRestrictionFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // More specific than the /api/auth/** permitAll below: changing
                        // your own password always requires being logged in as someone.
                        .requestMatchers("/api/auth/change-password").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/super-admin/auth/**").permitAll()
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")
                        // Razorpay cannot present a staff/super-admin JWT - this is instead
                        // secured by verifying the X-Razorpay-Signature header against the
                        // configured webhook secret (see PaymentWebhookController).
                        .requestMatchers("/api/payments/webhook").permitAll()
                        // Public "Request a Demo" lead capture - see EnquiryController.
                        // Anyone can submit it; the read-only session it hands back is
                        // then scoped/enforced by DemoAuthenticationFilter and
                        // DemoModeWriteRestrictionFilter below, not by a role check here.
                        .requestMatchers(HttpMethod.POST, "/api/enquiries").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Stays public: used both by the authenticated in-app "Ask AI
                        // Assistant" page (tenant resolved from the JWT, same as any other
                        // staff endpoint) and by anonymous patient-facing channels
                        // (WhatsApp/SMS/web widget) that have no staff login at all. See
                        // ChatController for how it resolves the tenant in the anonymous
                        // case (a required tenantSlug on the request).
                        .requestMatchers("/api/chat").permitAll()
                        // Read-only self-service view of the caller's own tenant's
                        // feature toggles/usage counters (see TenantSelfController) -
                        // an ADMIN-only concern, distinct from /api/super-admin/tenants
                        // which manages every tenant.
                        .requestMatchers("/api/tenant/**").hasRole("ADMIN")
                        // Staff/team management (list/deactivate/reset-password) within the
                        // caller's own clinic - see UserController. Creating a staff member
                        // is still POST /api/auth/register (unchanged).
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // "DEMO" (see DemoAuthenticationFilter) may read these same
                        // endpoints so a demo visitor sees the real, seeded clinic data;
                        // DemoModeWriteRestrictionFilter guarantees it can never write to
                        // them (or anything else).
                        .requestMatchers(HttpMethod.GET, "/api/patients/**", "/api/doctors/**", "/api/appointments/**")
                            .hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST", "DEMO")
                        // Non-GET (create/update): registering or editing patients/doctors
                        // is front-desk/admin work, not something a doctor does themselves.
                        .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers("/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(superAdminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(demoAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Must run after every authentication filter above (all three are
                // "before" UsernamePasswordAuthenticationFilter) so it sees whatever
                // authentication they set - placing it "after" that anchor guarantees
                // that ordering regardless of the three filters' relative order.
                .addFilterAfter(demoModeWriteRestrictionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
