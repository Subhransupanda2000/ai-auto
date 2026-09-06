package com.healthcareai.ai;

import java.util.List;

import com.healthcareai.entity.MessageRole;

/**
 * A single turn in a chat completion request/response, decoupled from any
 * specific LLM provider's wire format. Provider adapters (see
 * {@code com.healthcareai.integration}) translate to/from this model.
 */
public record ChatMessage(
        MessageRole role,
        String content,
        /** Present on TOOL role messages: the id of the tool call this message answers. */
        String toolCallId,
        /** Present on TOOL role messages: the name of the tool that was invoked. */
        String toolName,
        /** Present on ASSISTANT role messages when the model requests tool execution. */
        List<ToolCall> toolCalls
) {

    public static ChatMessage system(String content) {
        return new ChatMessage(MessageRole.SYSTEM, content, null, null, null);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage(MessageRole.USER, content, null, null, null);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage(MessageRole.ASSISTANT, content, null, null, null);
    }

    public static ChatMessage assistantToolRequest(List<ToolCall> toolCalls) {
        return new ChatMessage(MessageRole.ASSISTANT, null, null, null, toolCalls);
    }

    public static ChatMessage toolResult(String toolCallId, String toolName, String content) {
        return new ChatMessage(MessageRole.TOOL, content, toolCallId, toolName, null);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
