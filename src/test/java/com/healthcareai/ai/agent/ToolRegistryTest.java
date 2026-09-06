package com.healthcareai.ai.agent;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ChatMessage;
import com.healthcareai.ai.ToolCall;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.ai.tools.SimpleToolFunction;
import com.healthcareai.ai.tools.ToolProvider;
import com.healthcareai.entity.MessageRole;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void execute_invokesRegisteredFunction_andReturnsToolResultMessage() {
        ToolDefinition definition = new ToolDefinition("test_echo", "Echoes the given text.",
                "{\"type\":\"object\",\"properties\":{\"text\":{\"type\":\"string\"}}}");
        ToolProvider provider = () -> List.of(new SimpleToolFunction(definition,
                args -> "echo:" + args.get("text").asText()));

        ToolRegistry registry = new ToolRegistry(List.of(provider), objectMapper);

        ToolCall call = new ToolCall("call-1", "test_echo", "{\"text\":\"hello\"}");
        ChatMessage result = registry.execute(call);

        assertThat(result.role()).isEqualTo(MessageRole.TOOL);
        assertThat(result.toolCallId()).isEqualTo("call-1");
        assertThat(result.content()).isEqualTo("echo:hello");
    }

    @Test
    void execute_returnsErrorJson_forUnknownTool() {
        ToolRegistry registry = new ToolRegistry(List.of(), objectMapper);

        ChatMessage result = registry.execute(new ToolCall("call-2", "unknown_tool", "{}"));

        assertThat(result.content()).contains("error").contains("Unknown tool");
    }

    @Test
    void allDefinitions_includesEveryRegisteredFunction() {
        ToolDefinition definitionA = new ToolDefinition("tool_a", "desc", "{}");
        ToolDefinition definitionB = new ToolDefinition("tool_b", "desc", "{}");
        ToolProvider provider = () -> List.of(
                new SimpleToolFunction(definitionA, args -> "a"),
                new SimpleToolFunction(definitionB, args -> "b"));

        ToolRegistry registry = new ToolRegistry(List.of(provider), objectMapper);

        assertThat(registry.allDefinitions()).extracting(ToolDefinition::name)
                .containsExactlyInAnyOrder("tool_a", "tool_b");
    }
}
