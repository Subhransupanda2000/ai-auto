package com.healthcareai.ai.tools;

import java.util.List;

/**
 * Marker for a group of related {@link ToolFunction}s (e.g. all calendar
 * actions) contributed to the AI agent's tool registry.
 */
public interface ToolProvider {

    List<ToolFunction> getFunctions();
}
