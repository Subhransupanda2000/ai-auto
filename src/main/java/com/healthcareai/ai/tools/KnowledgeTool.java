package com.healthcareai.ai.tools;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.KnowledgeBaseService;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.entity.FaqDocument;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Exposes the RAG knowledge base to the AI agent so it can look up clinic
 * FAQs, doctor profiles, timings, insurance, and services on demand.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeTool implements ToolProvider {

    private final KnowledgeBaseService knowledgeBaseService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToolFunction> getFunctions() {
        return List.of(new SimpleToolFunction(searchKnowledgeBaseDefinition(), this::searchKnowledgeBase));
    }

    private ToolDefinition searchKnowledgeBaseDefinition() {
        return new ToolDefinition(
                "knowledge_searchKnowledgeBase",
                "Searches the clinic knowledge base (FAQs, doctor profiles, clinic timings, insurance information, "
                        + "medical services) for content relevant to a natural language query. Always use this before "
                        + "answering questions about the clinic.",
                """
                {
                  "type": "object",
                  "properties": {
                    "query": { "type": "string", "description": "The patient's question or topic to search for." },
                    "maxResults": { "type": "integer", "description": "Maximum number of results to return.", "default": 3 }
                  },
                  "required": ["query"]
                }
                """);
    }

    @SneakyThrows
    private String searchKnowledgeBase(JsonNode args) {
        String query = ToolArguments.requiredString(args, "query");
        int maxResults = args.has("maxResults") ? args.get("maxResults").asInt(3) : 3;

        List<FaqDocument> results = knowledgeBaseService.retrieveRelevant(query, maxResults);
        List<KnowledgeResult> payload = results.stream()
                .map(doc -> new KnowledgeResult(doc.getCategory().name(), doc.getTitle(), doc.getContent()))
                .toList();
        return objectMapper.writeValueAsString(payload);
    }

    private record KnowledgeResult(String category, String title, String content) {
    }
}
