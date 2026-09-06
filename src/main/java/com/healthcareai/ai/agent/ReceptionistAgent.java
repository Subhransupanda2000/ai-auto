package com.healthcareai.ai.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.healthcareai.ai.ChatCompletionResult;
import com.healthcareai.ai.ChatMessage;
import com.healthcareai.ai.KnowledgeBaseService;
import com.healthcareai.ai.ToolCall;
import com.healthcareai.ai.prompts.SystemPromptProvider;
import com.healthcareai.entity.Channel;
import com.healthcareai.entity.Conversation;
import com.healthcareai.entity.FaqDocument;
import com.healthcareai.entity.MessageRole;
import com.healthcareai.exception.LlmServiceException;
import com.healthcareai.integration.GeminiClient;
import com.healthcareai.service.ConversationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The AI Receptionist: orchestrates a single conversational turn by
 * combining the system prompt, RAG-retrieved clinic knowledge, bounded
 * conversation history, and tool calling into a chat-completion loop.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReceptionistAgent {

    private static final int MAX_TOOL_ITERATIONS = 5;
    private static final int MAX_CONTEXT_MESSAGES = 20;
    private static final int MAX_KNOWLEDGE_RESULTS = 4;

    private final GeminiClient geminiClient;
    private final KnowledgeBaseService knowledgeBaseService;
    private final SystemPromptProvider systemPromptProvider;
    private final ConversationService conversationService;
    private final ToolRegistry toolRegistry;

    /**
     * Handles one user message for the given session, returning the
     * assistant's final natural-language reply. The user message and the
     * assistant's reply are persisted as conversation history; intermediate
     * tool-calling exchanges are not persisted (they are reconstructed fresh
     * on each turn from the current conversation history and RAG context).
     */
    public String handleUserMessage(String sessionId, Channel channel, UUID patientId, String userMessage) {
        conversationService.logMessage(sessionId, channel, MessageRole.USER, userMessage, null, patientId);

        List<FaqDocument> knowledge = knowledgeBaseService.retrieveRelevant(userMessage, MAX_KNOWLEDGE_RESULTS);
        String systemPrompt = systemPromptProvider.build(knowledge);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        messages.addAll(loadHistoryAsChatMessages(sessionId));

        // TEMPORARY DEBUG (remove after root-causing the Gemini failure):
        // rethrow instead of swallowing into the generic fallback message so
        // the real exception/stack trace surfaces via GlobalExceptionHandler
        // and the server logs, instead of being hidden behind a 200/503
        // fallback response.
        String finalReply;
        try {
            finalReply = runCompletionLoop(messages);
        } catch (LlmServiceException e) {
            log.error("AI receptionist failed to produce a response for session {}. Full exception below.", sessionId, e);
            Throwable cause = e.getCause();
            if (cause != null) {
                log.error("Root cause of LlmServiceException: {}: {}", cause.getClass().getName(), cause.getMessage(), cause);
            }
            throw e; // TEMPORARY DEBUG: was swallowed into a fallback reply + escalate() call.
        }

        conversationService.logMessage(sessionId, channel, MessageRole.ASSISTANT, finalReply, null, patientId);
        return finalReply;
    }

    private String runCompletionLoop(List<ChatMessage> messages) {
        List<com.healthcareai.ai.ToolDefinition> tools = toolRegistry.allDefinitions();

        for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
            ChatCompletionResult result = geminiClient.createChatCompletion(messages, tools);

            if (!result.requiresToolExecution()) {
                return result.message().content() != null
                        ? result.message().content()
                        : "I'm sorry, could you rephrase that?";
            }

            messages.add(result.message());
            for (ToolCall toolCall : result.message().toolCalls()) {
                ChatMessage toolResult = toolRegistry.execute(toolCall);
                messages.add(toolResult);
            }
        }

        log.warn("AI receptionist exceeded {} tool-calling iterations; returning a fallback response.", MAX_TOOL_ITERATIONS);
        return "I need a bit more help to complete this request. Let me connect you with a member of our team.";
    }

    private List<ChatMessage> loadHistoryAsChatMessages(String sessionId) {
        List<Conversation> history = conversationService.getRecentContext(sessionId, MAX_CONTEXT_MESSAGES);
        return history.stream()
                .filter(c -> c.getRole() == MessageRole.USER || c.getRole() == MessageRole.ASSISTANT)
                .map(c -> new ChatMessage(c.getRole(), c.getMessage(), null, null, null))
                .toList();
    }
}
