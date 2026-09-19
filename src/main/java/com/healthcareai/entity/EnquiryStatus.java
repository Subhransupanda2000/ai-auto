package com.healthcareai.entity;

/**
 * Sales-follow-up lifecycle of an {@link Enquiry} raised via the public
 * "Request a Demo" form.
 */
public enum EnquiryStatus {
    NEW,
    CONTACTED,
    CONVERTED,
    CLOSED
}
