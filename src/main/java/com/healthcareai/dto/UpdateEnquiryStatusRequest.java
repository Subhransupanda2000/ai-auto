package com.healthcareai.dto;

import jakarta.validation.constraints.NotNull;

import com.healthcareai.entity.EnquiryStatus;

public record UpdateEnquiryStatusRequest(@NotNull EnquiryStatus status) {
}
