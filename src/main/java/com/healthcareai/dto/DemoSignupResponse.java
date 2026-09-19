package com.healthcareai.dto;

/**
 * Returned by {@code POST /api/enquiries}: the same shape as {@link
 * LoginResponse} (minus a refresh token - demo sessions simply expire,
 * they're never silently renewed) so the frontend can drop it straight
 * into its existing session store and land the visitor in the real app
 * UI, read-only, over the seeded demo tenant.
 */
public record DemoSignupResponse(EnquiryResponse enquiry, String accessToken, String tokenType, long expiresInSeconds) {

    public static DemoSignupResponse of(EnquiryResponse enquiry, String accessToken, long expiresInSeconds) {
        return new DemoSignupResponse(enquiry, accessToken, "Bearer", expiresInSeconds);
    }
}
