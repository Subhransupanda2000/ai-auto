package com.healthcareai.integration;

import java.util.List;

import com.healthcareai.ai.ChatCompletionResult;
import com.healthcareai.ai.ChatMessage;
import com.healthcareai.ai.ToolDefinition;

/**
 * Abstraction over the Google Gemini {@code generateContent} (chat/tool
 * calling) and {@code embedContent} (embeddings) REST APIs. Kept behind an
 * interface so the LLM provider can be swapped (or mocked in tests)
 * without touching the AI orchestration layer.
 */
public interface GeminiClient {

    /**
     * Requests a chat completion, optionally offering a set of callable
     * tools. The model may respond with plain text or with one or more
     * tool calls (see {@link ChatCompletionResult#requiresToolExecution()}).
     */
    ChatCompletionResult createChatCompletion(List<ChatMessage> messages, List<ToolDefinition> tools);

    /**
     * Computes an embedding vector for the given text, using the configured
     * embedding model.
     */
    float[] createEmbedding(String text);
}
