package com.healthcareai.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTenantWhatsappRequest(@NotNull Boolean enabled) {
}
