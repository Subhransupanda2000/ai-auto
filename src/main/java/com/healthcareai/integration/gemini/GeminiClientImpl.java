package com.healthcareai.integration.gemini;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.healthcareai.ai.ChatCompletionResult;
import com.healthcareai.ai.ChatMessage;
import com.healthcareai.ai.ToolCall;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.config.GeminiProperties;
import com.healthcareai.entity.MessageRole;
import com.healthcareai.exception.LlmServiceException;
import com.healthcareai.integration.GeminiClient;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiContent;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiFunctionCall;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiFunctionDeclaration;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiFunctionResponse;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiGenerationConfig;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiPart;
import com.healthcareai.integration.gemini.GeminiGenerateContentRequest.GeminiTool;
import com.healthcareai.integration.gemini.GeminiGenerateContentResponse.Candidate;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link GeminiClient} implementation backed by the Google Gemini REST API
 * ({@code generateContent} for chat/tool-calling, {@code embedContent} for
 * embeddings). No Google SDK is used; requests are built and parsed
 * directly against the documented JSON wire format.
 */
@Component
@Slf4j
public class GeminiClientImpl implements GeminiClient {

    private static final String MODEL_ROLE = "model";
    private static final String USER_ROLE = "user";
    // NOTE: the classic "function" role (used in older Gemini function-calling
    // examples/cookbooks) is rejected by the current generateContent API for
    // this model with: "Role 'function' is not supported. Please use a valid
    // role: SYSTEM, ..., USER, ASSISTANT, ..., MODEL, USER." (confirmed by
    // live testing). Function/tool results are sent back as role "user".
    private static final String FUNCTION_ROLE = USER_ROLE;

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;
    private final GeminiProperties properties;

    public GeminiClientImpl(@Qualifier("geminiRestClient") RestClient geminiRestClient,
                             ObjectMapper objectMapper,
                             GeminiProperties properties) {
        this.geminiRestClient = geminiRestClient;
        this.objectMapper = objectMapper;
        this.properties = properties;

        boolean apiKeyPresent = properties.apiKey() != null && !properties.apiKey().isBlank();
        log.info("Gemini AI client configured (api-key-present={}, base-url={}, chat-model={}, embedding-model={}, "
                        + "embedding-dimensions={}, timeout-seconds={})",
                apiKeyPresent, properties.baseUrl(), properties.model(), properties.embeddingModel(),
                properties.embeddingDimensions(), properties.timeoutSeconds());
        if (!apiKeyPresent) {
            log.warn("No Gemini API key configured (app.gemini.api-key / GEMINI_API_KEY is empty). "
                    + "Every request to Gemini will fail authentication until this is set.");
        }
    }

    @Override
    @Retryable(retryFor = RestClientException.class, noRetryFor = HttpClientErrorException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public ChatCompletionResult createChatCompletion(List<ChatMessage> messages, List<ToolDefinition> tools) {
        String uri = "/models/{model}:generateContent";
        try {
            GeminiContent systemInstruction = extractSystemInstruction(messages);
            List<GeminiContent> contents = toGeminiContents(messages);
            List<GeminiTool> geminiTools = toGeminiTools(tools);

            GeminiGenerateContentRequest request = new GeminiGenerateContentRequest(
                    contents,
                    systemInstruction,
                    geminiTools,
                    new GeminiGenerationConfig(properties.temperature()));

            logOutgoingRequest("generateContent", uri, properties.model(), request);

            String rawResponse = geminiRestClient.post()
                    .uri(uri, properties.model())
                    .body(request)
                    .retrieve()
                    .body(String.class);

            log.debug("Gemini generateContent response <- {}", rawResponse);

            GeminiGenerateContentResponse response = objectMapper.readValue(rawResponse, GeminiGenerateContentResponse.class);

            if (response.candidates() == null || response.candidates().isEmpty()) {
                throw new LlmServiceException("Gemini returned no candidates for the chat completion request. Raw response: " + rawResponse);
            }

            Candidate candidate = response.candidates().get(0);
            return new ChatCompletionResult(toChatMessage(candidate), candidate.finishReason());
        } catch (RestClientResponseException e) {
            logHttpError("generateContent", properties.model(), e);
            throw new LlmServiceException(
                    "Gemini API error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("Gemini generateContent call failed (no HTTP response received, e.g. timeout or connection error) "
                    + "for model={}", properties.model(), e);
            throw new LlmServiceException("Failed to obtain a response from the AI model.", e);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini generateContent response body for model={}", properties.model(), e);
            throw new LlmServiceException("Failed to parse the AI model's response.", e);
        }
    }

    @Override
    @Retryable(retryFor = RestClientException.class, noRetryFor = HttpClientErrorException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public float[] createEmbedding(String text) {
        String uri = "/models/{model}:embedContent";
        try {
            GeminiEmbedContentRequest request = new GeminiEmbedContentRequest(
                    new GeminiEmbedContentRequest.GeminiEmbedContentPart(
                            List.of(new GeminiEmbedContentRequest.GeminiTextPart(text))),
                    properties.embeddingDimensions());

            logOutgoingRequest("embedContent", uri, properties.embeddingModel(), request);

            String rawResponse = geminiRestClient.post()
                    .uri(uri, properties.embeddingModel())
                    .body(request)
                    .retrieve()
                    .body(String.class);

            log.debug("Gemini embedContent response <- {}", rawResponse);

            GeminiEmbedContentResponse response = objectMapper.readValue(rawResponse, GeminiEmbedContentResponse.class);

            if (response.embedding() == null || response.embedding().values() == null) {
                throw new LlmServiceException("Gemini returned no embedding data. Raw response: " + rawResponse);
            }
            List<Float> values = response.embedding().values();
            float[] result = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = values.get(i);
            }
            return result;
        } catch (RestClientResponseException e) {
            logHttpError("embedContent", properties.embeddingModel(), e);
            throw new LlmServiceException(
                    "Gemini API error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("Gemini embedContent call failed (no HTTP response received, e.g. timeout or connection error) "
                    + "for model={}", properties.embeddingModel(), e);
            throw new LlmServiceException("Failed to compute an embedding for the given text.", e);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini embedContent response body for model={}", properties.embeddingModel(), e);
            throw new LlmServiceException("Failed to parse the AI model's embedding response.", e);
        }
    }

    private void logOutgoingRequest(String operation, String uriTemplate, String model, Object requestBody) {
        if (!log.isDebugEnabled()) {
            return;
        }
        try {
            log.debug("Gemini {} request -> url={}{} (model={}, api-key={}) body={}",
                    operation, properties.baseUrl(), uriTemplate.replace("{model}", model), model,
                    maskApiKey(properties.apiKey()), objectMapper.writeValueAsString(requestBody));
        } catch (JsonProcessingException e) {
            log.debug("Gemini {} request -> (failed to serialize request body for logging: {})", operation, e.getMessage());
        }
    }

    private void logHttpError(String operation, String model, RestClientResponseException e) {
        log.error("Gemini {} call failed for model={}: status={} ({}), responseHeaders={}, responseBody={}",
                operation, model, e.getStatusCode().value(), e.getStatusText(),
                e.getResponseHeaders(), e.getResponseBodyAsString());
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "<missing>";
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    /** Gemini has no "system" role in {@code contents}; the first SYSTEM message becomes {@code systemInstruction}. */
    private GeminiContent extractSystemInstruction(List<ChatMessage> messages) {
        return messages.stream()
                .filter(m -> m.role() == MessageRole.SYSTEM)
                .findFirst()
                .map(m -> new GeminiContent(null, List.of(GeminiPart.ofText(m.content()))))
                .orElse(null);
    }

    private List<GeminiContent> toGeminiContents(List<ChatMessage> messages) {
        List<GeminiContent> contents = new ArrayList<>();
        for (ChatMessage message : messages) {
            switch (message.role()) {
                case SYSTEM -> {
                    // handled separately via systemInstruction
                }
                case USER -> contents.add(new GeminiContent(USER_ROLE, List.of(GeminiPart.ofText(message.content()))));
                case ASSISTANT -> contents.add(toAssistantContent(message));
                case TOOL -> contents.add(toFunctionResponseContent(message));
            }
        }
        return contents;
    }

    private GeminiContent toAssistantContent(ChatMessage message) {
        if (message.hasToolCalls()) {
            // Gemini 2.5/3 "thinking" models require both the functionCall's `id`
            // and the (sibling, part-level) `thoughtSignature` to be echoed back
            // verbatim when replaying a functionCall part in history, or the
            // request fails with 400 "Function call is missing a
            // thought_signature". Neither fits the generic ToolCall(id, name,
            // argumentsJson) shape used by the tool-calling architecture, so both
            // are packed into ToolCall.id() (see toChatMessage) and unpacked here.
            List<GeminiPart> parts = message.toolCalls().stream()
                    .map(tc -> GeminiPart.ofFunctionCall(
                            new GeminiFunctionCall(tc.toolName(), parseArgs(tc.argumentsJson()), unpackFunctionCallId(tc.id())),
                            unpackThoughtSignature(tc.id())))
                    .toList();
            return new GeminiContent(MODEL_ROLE, parts);
        }
        return new GeminiContent(MODEL_ROLE, List.of(GeminiPart.ofText(message.content())));
    }

    private GeminiContent toFunctionResponseContent(ChatMessage message) {
        Object responsePayload = toFunctionResponsePayload(message.content());
        GeminiFunctionResponse functionResponse = new GeminiFunctionResponse(
                message.toolName(), responsePayload, unpackFunctionCallId(message.toolCallId()));
        return new GeminiContent(FUNCTION_ROLE, List.of(GeminiPart.ofFunctionResponse(functionResponse)));
    }

    // --- ToolCall.id() packing helpers -------------------------------------
    // Encoding: "<functionCallId>\u0001<thoughtSignature>", either half may be
    // empty. Confined entirely to this class; the generic ToolCall/ToolRegistry
    // types are untouched and treat this as an opaque id string.
    private static final String ID_SIG_DELIMITER = "\u0001";

    private String packFunctionCallId(String functionCallId, String thoughtSignature) {
        return (functionCallId == null ? "" : functionCallId) + ID_SIG_DELIMITER + (thoughtSignature == null ? "" : thoughtSignature);
    }

    private String unpackFunctionCallId(String packed) {
        if (packed == null || packed.isBlank()) {
            return null;
        }
        int idx = packed.indexOf(ID_SIG_DELIMITER);
        String id = idx >= 0 ? packed.substring(0, idx) : packed;
        return id.isBlank() ? null : id;
    }

    private String unpackThoughtSignature(String packed) {
        if (packed == null) {
            return null;
        }
        int idx = packed.indexOf(ID_SIG_DELIMITER);
        String sig = idx >= 0 ? packed.substring(idx + 1) : "";
        return sig.isBlank() ? null : sig;
    }

    /**
     * Gemini's {@code functionResponse.response} field is a proto struct and
     * must be a JSON <em>object</em> - sending a JSON array (as several of
     * our tools return, e.g. {@code knowledge_searchKnowledgeBase} or
     * {@code appointment_findAppointment} listing multiple results) is
     * rejected with {@code "Proto field is not repeating, cannot start
     * list."}. Arrays/primitives/plain strings are therefore wrapped in a
     * single-field object; JSON objects are passed through unchanged.
     */
    private Object toFunctionResponsePayload(String content) {
        Object parsed = parseArgs(content);
        if (parsed instanceof JsonNode node && node.isObject()) {
            return node;
        }
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set("result", parsed instanceof JsonNode node ? node : objectMapper.valueToTree(parsed));
        return wrapper;
    }

    private List<GeminiTool> toGeminiTools(List<ToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return null;
        }
        List<GeminiFunctionDeclaration> declarations = tools.stream()
                .map(tool -> new GeminiFunctionDeclaration(tool.name(), tool.description(), parseJsonSchema(tool.parametersJsonSchema())))
                .toList();
        return List.of(new GeminiTool(declarations));
    }

    private Object parseArgs(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            // Not JSON (e.g. a plain-text tool result) - wrap it so Gemini still receives an object.
            return objectMapper.createObjectNode().put("result", json);
        }
    }

    private JsonNode parseJsonSchema(String parametersJsonSchema) {
        try {
            return objectMapper.readTree(parametersJsonSchema);
        } catch (JsonProcessingException e) {
            throw new LlmServiceException("Invalid tool parameter JSON schema.", e);
        }
    }

    private ChatMessage toChatMessage(Candidate candidate) {
        List<GeminiGenerateContentResponse.Part> parts = candidate.content() != null ? candidate.content().parts() : null;
        if (parts == null || parts.isEmpty()) {
            return ChatMessage.assistant(null);
        }

        List<ToolCall> toolCalls = parts.stream()
                .filter(part -> part.functionCall() != null)
                .map(part -> new ToolCall(
                        // Carries both the functionCall's own `id` and the sibling
                        // part-level `thoughtSignature` (only the first functionCall
                        // part in a parallel-call response is guaranteed to have a
                        // signature - see toAssistantContent/unpack* for the reverse).
                        packFunctionCallId(part.functionCall().id(), part.thoughtSignature()),
                        part.functionCall().name(),
                        writeArgsAsJson(part.functionCall().args())))
                .toList();

        if (!toolCalls.isEmpty()) {
            return ChatMessage.assistantToolRequest(toolCalls);
        }

        String text = parts.stream()
                .map(GeminiGenerateContentResponse.Part::text)
                .filter(t -> t != null && !t.isBlank())
                .reduce("", (a, b) -> a.isBlank() ? b : a + "\n" + b);
        return ChatMessage.assistant(text.isBlank() ? null : text);
    }

    private String writeArgsAsJson(JsonNode args) {
        try {
            return objectMapper.writeValueAsString(args == null ? objectMapper.createObjectNode() : args);
        } catch (JsonProcessingException e) {
            throw new LlmServiceException("Failed to serialize Gemini function call arguments.", e);
        }
    }
}
