package com.healthcareai.ai;

/**
 * Describes a callable tool/function to the LLM: its name, a natural
 * language description (which the model uses to decide when to call it),
 * and a JSON Schema (as a raw JSON string) describing its parameters.
 */
public record ToolDefinition(String name, String description, String parametersJsonSchema) {
}
