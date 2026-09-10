package com.healthcareai.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueResponse(
        RevenueRange range,
        BigDecimal totalRevenue,
        long completedAppointments,
        Instant rangeStart,
        Instant rangeEnd
) {
}
