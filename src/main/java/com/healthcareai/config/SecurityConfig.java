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
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Stays public: used both by the authenticated in-app "Ask AI
                        // Assistant" page (tenant resolved from the JWT, same as any other
                        // staff endpoint) and by anonymous patient-facing channels
                        // (WhatsApp/SMS/web widget) that have no staff login at all. See
                        // ChatController for how it resolves the tenant in the anonymous
                        // case (a required tenantSlug on the request).
                        .requestMatchers("/api/chat").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/patients/**", "/api/doctors/**", "/api/appointments/**")
                            .hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        // Non-GET (create/update): registering or editing patients/doctors
                        // is front-desk/admin work, not something a doctor does themselves.
                        .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers("/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(superAdminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
