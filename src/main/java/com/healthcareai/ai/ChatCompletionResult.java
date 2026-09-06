package com.healthcareai.ai;

/**
 * Result of a single chat completion call.
 */
public record ChatCompletionResult(ChatMessage message, String finishReason) {

    public boolean requiresToolExecution() {
        return message != null && message.hasToolCalls();
    }
}
