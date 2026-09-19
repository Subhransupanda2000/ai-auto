package com.healthcareai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public "Request a Demo" form submission - see {@code EnquiryController}.
 */
public record CreateEnquiryRequest(
        @NotBlank @Size(max = 255) String fullName,

        @NotBlank @Email @Size(max = 255) String email,

        @Size(max = 50) String phone,

        @Size(max = 255) String clinicName,

        @Size(max = 2000) String message
) {
}
