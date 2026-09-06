package com.healthcareai.ai.tools;

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthcareai.ai.ToolDefinition;

/**
 * Convenience {@link ToolFunction} implementation backed by a definition and
 * an executor function, so concrete tool classes can be written declaratively.
 */
public class SimpleToolFunction implements ToolFunction {

    private final ToolDefinition definition;
    private final Function<JsonNode, String> executor;

    public SimpleToolFunction(ToolDefinition definition, Function<JsonNode, String> executor) {
        this.definition = definition;
        this.executor = executor;
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    @Override
    public String execute(JsonNode arguments) {
        return executor.apply(arguments);
    }
}
