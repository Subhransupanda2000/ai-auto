package com.healthcareai.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTenantAiChatRequest(@NotNull Boolean enabled) {
}
