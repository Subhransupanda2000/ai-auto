package com.healthcareai.ai.agent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ChatMessage;
import com.healthcareai.ai.ToolCall;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.ai.tools.ToolFunction;
import com.healthcareai.ai.tools.ToolProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Aggregates every {@link ToolProvider} in the application context into a
 * single name-addressable registry, and safely executes tool calls
 * requested by the LLM, translating any failure into a tool-result message
 * the model can react to instead of crashing the conversation.
 */
@Component
@Slf4j
public class ToolRegistry {

    private final Map<String, ToolFunction> functionsByName;
    private final ObjectMapper objectMapper;

    public ToolRegistry(List<ToolProvider> providers, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.functionsByName = providers.stream()
                .flatMap(provider -> provider.getFunctions().stream())
                .collect(Collectors.toMap(fn -> fn.definition().name(), fn -> fn));
        log.info("Registered {} AI tool functions: {}", functionsByName.size(), functionsByName.keySet());
    }

    public List<ToolDefinition> allDefinitions() {
        return functionsByName.values().stream().map(ToolFunction::definition).toList();
    }

    /**
     * Executes the given tool call and returns the corresponding TOOL role
     * message to append to the conversation. Never throws: execution errors
     * are surfaced to the model as a JSON error payload so it can recover
     * gracefully (e.g. ask a follow-up question or escalate).
     */
    public ChatMessage execute(ToolCall toolCall) {
        ToolFunction function = functionsByName.get(toolCall.toolName());
        if (function == null) {
            return ChatMessage.toolResult(toolCall.id(), toolCall.toolName(),
                    errorJson("Unknown tool: " + toolCall.toolName()));
        }
        try {
            JsonNode arguments = objectMapper.readTree(
                    toolCall.argumentsJson() == null || toolCall.argumentsJson().isBlank()
                            ? "{}" : toolCall.argumentsJson());
            String result = function.execute(arguments);
            return ChatMessage.toolResult(toolCall.id(), toolCall.toolName(), result);
        } catch (Exception e) {
            log.warn("Tool execution failed for {}: {}", toolCall.toolName(), e.getMessage());
            return ChatMessage.toolResult(toolCall.id(), toolCall.toolName(), errorJson(e.getMessage()));
        }
    }

    private String errorJson(String message) {
        return "{\"error\": \"" + (message == null ? "Tool execution failed." : message.replace("\"", "'")) + "\"}";
    }
}
