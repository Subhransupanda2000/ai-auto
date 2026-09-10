package com.healthcareai.dto;

/**
 * Rolling date-range options for {@code GET /api/appointments/revenue},
 * each a trailing window ending "today" (inclusive) in the clinic's
 * timezone - see {@code AppointmentServiceImpl.resolveRevenueWindow}.
 */
public enum RevenueRange {
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    LAST_MONTH,
    LAST_6_MONTHS,
    LAST_YEAR
}
