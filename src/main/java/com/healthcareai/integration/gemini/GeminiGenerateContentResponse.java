package com.healthcareai.integration.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Wire-format response body for
 * {@code POST /models/{model}:generateContent}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiGenerateContentResponse(List<Candidate> candidates) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content, @JsonProperty("finishReason") String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(String role, List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(
            String text,
            @JsonProperty("functionCall") FunctionCall functionCall,
            // Sibling of functionCall, not nested inside it - see
            // https://ai.google.dev/gemini-api/docs/generate-content/thought-signatures
            @JsonProperty("thoughtSignature") String thoughtSignature
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FunctionCall(String name, JsonNode args, String id) {
    }
}
