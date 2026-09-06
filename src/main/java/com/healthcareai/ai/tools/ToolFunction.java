package com.healthcareai.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthcareai.ai.ToolDefinition;

/**
 * A single callable function exposed to the LLM via tool/function calling.
 */
public interface ToolFunction {

    ToolDefinition definition();

    /**
     * Executes the function with the arguments the model supplied, and
     * returns a plain-text/JSON result to be fed back to the model as the
     * tool call's result content.
     */
    String execute(JsonNode arguments);
}
