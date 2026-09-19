package com.healthcareai.security.demo;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.dto.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Hard safety net for the read-only demo preview: rejects every mutating
 * request (anything but GET/HEAD/OPTIONS) from a {@code ROLE_DEMO}
 * principal with a friendly 403, regardless of which endpoint it targets.
 * Path-specific role checks in {@code SecurityConfig} only cover the
 * endpoints that already had role restrictions before demo mode existed
 * (patients/doctors/appointments); this filter is the actual guarantee
 * that a demo visitor can never create/update/delete anything anywhere,
 * including endpoints that are merely {@code anyRequest().authenticated()}
 * today. Runs after every authentication filter (see {@code
 * SecurityConfig}) so {@code SecurityContextHolder} is already populated.
 */
@Component
@RequiredArgsConstructor
public class DemoModeWriteRestrictionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isDemo = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DEMO"::equals);
        boolean isWrite = !HttpMethod.GET.matches(request.getMethod())
                && !HttpMethod.HEAD.matches(request.getMethod())
                && !HttpMethod.OPTIONS.matches(request.getMethod());

        if (isDemo && isWrite) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse body = ErrorResponse.of(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    "This is a read-only demo account. Sign up for a real clinic account to make changes.",
                    request.getRequestURI());
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
