package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Google Gemini integration (chat/tool-calling via
 * {@code generateContent}, and embeddings via {@code embedContent}), used
 * behind {@code com.healthcareai.integration.GeminiClient}.
 */
@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        int timeoutSeconds,
        double temperature,
        String embeddingModel,
        int embeddingDimensions
) {

    public GeminiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        }
        if (model == null || model.isBlank()) {
            // "gemini-flash-latest" is a stable alias Google keeps pointed at a
            // currently-available flash model; pinned versions like
            // "gemini-2.5-flash" can return 404 "no longer available to new
            // users" depending on when the API key/project was created.
            model = "gemini-flash-latest";
        }
        if (embeddingModel == null || embeddingModel.isBlank()) {
            embeddingModel = "gemini-embedding-001";
        }
        if (embeddingDimensions <= 0) {
            // Must match the vector(N) dimension declared in
            // V7__create_faq_documents_table.sql. gemini-embedding-001
            // supports Matryoshka truncation to 768/1536/3072; 1536 keeps
            // the existing pgvector column unchanged.
            embeddingDimensions = 1536;
        }
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 30;
        }
    }
}
