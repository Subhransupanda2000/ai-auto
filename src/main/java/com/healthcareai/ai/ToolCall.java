package com.healthcareai.ai;

/**
 * A request from the model to invoke a tool, with its arguments encoded as
 * a raw JSON object string (as returned by the LLM provider).
 */
public record ToolCall(String id, String toolName, String argumentsJson) {
}
