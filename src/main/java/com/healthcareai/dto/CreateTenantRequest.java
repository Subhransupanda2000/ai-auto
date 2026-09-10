package com.healthcareai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Super-admin request to onboard a new clinic tenant along with its first
 * (ADMIN) staff user.
 */
public record CreateTenantRequest(
        @NotBlank String tenantName,

        @NotBlank
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "Slug must be lowercase letters, numbers, and hyphens only, e.g. 'sunrise-clinic'.")
        String slug,

        @NotBlank String adminFullName,

        @NotBlank @Email String adminEmail,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters.") String adminPassword
) {
}
