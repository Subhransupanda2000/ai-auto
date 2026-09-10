package com.healthcareai.security.superadmin;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.healthcareai.entity.SuperAdmin;
import com.healthcareai.repository.SuperAdminRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts and validates a super-admin {@code Authorization: Bearer <token>}
 * header, populating the Spring Security context with a {@code
 * ROLE_SUPER_ADMIN} authority. Entirely independent from {@link
 * com.healthcareai.security.JwtAuthenticationFilter}: a staff token simply
 * fails {@link SuperAdminJwtService#parseSuperAdminClaims} and is ignored
 * here (and a super-admin token is, symmetrically, rejected by the staff
 * {@code CustomUserDetailsService} lookup since no matching {@code User}
 * row exists), so both filters can run on every request with no shared
 * state or ordering dependency.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SuperAdminJwtService jwtService;
    private final SuperAdminRepository superAdminRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                String email = jwtService.extractEmail(token);
                SuperAdmin superAdmin = superAdminRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new IllegalStateException("No super admin found with email " + email));

                var authentication = new UsernamePasswordAuthenticationToken(
                        superAdmin.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException e) {
                log.debug("Ignoring invalid super-admin JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
