package com.healthcareai.integration.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wire-format response body for {@code POST /models/{model}:embedContent}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiEmbedContentResponse(GeminiEmbedding embedding) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiEmbedding(List<Float> values) {
    }
}
