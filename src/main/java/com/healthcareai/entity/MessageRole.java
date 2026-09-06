package com.healthcareai.entity;

/**
 * The role of the speaker of a single {@link Conversation} message, mirroring
 * the roles used in LLM chat completion APIs.
 */
public enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}
