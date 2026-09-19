package com.healthcareai.security.demo;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.healthcareai.tenant.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts and validates a demo-preview {@code Authorization: Bearer <token>}
 * header, populating the Spring Security context with a {@code ROLE_DEMO}
 * authority and {@code TenantContext} with the (seeded) demo tenant the
 * token is scoped to - there is no persisted account behind it, unlike
 * {@code JwtAuthenticationFilter}. Entirely independent from staff/
 * super-admin authentication: a staff or super-admin token simply fails
 * {@link DemoJwtService#parseDemoClaims} and is ignored here. Every
 * write (non-GET) request from a {@code ROLE_DEMO} principal is then
 * rejected by {@code DemoModeWriteRestrictionFilter}, which runs after
 * this one.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final DemoJwtService demoJwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                String email = demoJwtService.extractEmail(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_DEMO")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                TenantContext.setCurrentTenantId(demoJwtService.extractTenantId(token));
            } catch (RuntimeException e) {
                log.debug("Ignoring invalid demo JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
