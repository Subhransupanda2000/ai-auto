package com.healthcareai.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
 * Extracts and validates the {@code Authorization: Bearer <token>} header on
 * every request, populating the Spring Security context when valid. Absent
 * or invalid tokens are simply ignored here; endpoints that require
 * authentication will then be rejected by the security filter chain itself.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        try {
            if (header != null && header.startsWith(BEARER_PREFIX) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String token = header.substring(BEARER_PREFIX.length());
                try {
                    String email = jwtService.extractEmail(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // The tenantId claim is trusted directly from the (signature-verified)
                    // token, avoiding an extra lookup just to scope the rest of the request.
                    TenantContext.setCurrentTenantId(jwtService.extractTenantId(token));
                } catch (RuntimeException e) {
                    log.debug("Ignoring invalid JWT: {}", e.getMessage());
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            // Threads are pooled, so always clear the tenant context at the end of
            // the request regardless of outcome to avoid leaking it into whatever
            // the next request handled by this thread happens to be.
            TenantContext.clear();
        }
    }
}
