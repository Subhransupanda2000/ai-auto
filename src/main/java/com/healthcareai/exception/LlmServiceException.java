package com.healthcareai.exception;

/**
 * Thrown when the LLM provider (OpenAI) call fails, times out, or returns
 * an unusable response. Callers should catch this to apply a graceful
 * fallback (e.g. "please try again" or escalate to a human receptionist).
 */
public class LlmServiceException extends RuntimeException {

    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmServiceException(String message) {
        super(message);
    }
}
