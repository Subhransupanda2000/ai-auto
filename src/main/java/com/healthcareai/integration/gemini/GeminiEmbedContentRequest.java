package com.healthcareai.integration.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire-format request body for {@code POST /models/{model}:embedContent}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiEmbedContentRequest(
        GeminiEmbedContentPart content,
        @JsonProperty("outputDimensionality") Integer outputDimensionality
) {

    public record GeminiEmbedContentPart(List<GeminiTextPart> parts) {
    }

    public record GeminiTextPart(String text) {
    }
}
