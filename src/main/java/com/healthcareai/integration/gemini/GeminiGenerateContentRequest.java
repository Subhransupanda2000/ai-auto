package com.healthcareai.integration.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire-format request body for
 * {@code POST /models/{model}:generateContent}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerateContentRequest(
        List<GeminiContent> contents,
        @JsonProperty("systemInstruction") GeminiContent systemInstruction,
        List<GeminiTool> tools,
        @JsonProperty("generationConfig") GeminiGenerationConfig generationConfig
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiContent(String role, List<GeminiPart> parts) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiPart(
            String text,
            @JsonProperty("functionCall") GeminiFunctionCall functionCall,
            @JsonProperty("functionResponse") GeminiFunctionResponse functionResponse,
            // Sibling of functionCall (NOT nested inside it) - required by
            // Gemini's "thinking" models (2.5/3 series) when replaying a
            // functionCall part back in conversation history. See
            // https://ai.google.dev/gemini-api/docs/generate-content/thought-signatures
            @JsonProperty("thoughtSignature") String thoughtSignature
    ) {
        public static GeminiPart ofText(String text) {
            return new GeminiPart(text, null, null, null);
        }

        public static GeminiPart ofFunctionCall(GeminiFunctionCall functionCall, String thoughtSignature) {
            return new GeminiPart(null, functionCall, null, thoughtSignature);
        }

        public static GeminiPart ofFunctionResponse(GeminiFunctionResponse functionResponse) {
            return new GeminiPart(null, null, functionResponse, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiFunctionCall(String name, Object args, String id) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiFunctionResponse(String name, Object response, String id) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiTool(@JsonProperty("functionDeclarations") List<GeminiFunctionDeclaration> functionDeclarations) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiFunctionDeclaration(String name, String description, Object parameters) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiGenerationConfig(Double temperature) {
    }
}
