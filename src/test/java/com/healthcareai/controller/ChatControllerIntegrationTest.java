package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.healthcareai.AbstractIntegrationTest;
import com.healthcareai.ai.ChatCompletionResult;
import com.healthcareai.ai.ChatMessage;
import com.healthcareai.dto.ChatRequest;
import com.healthcareai.dto.ChatResponse;
import com.healthcareai.entity.Conversation;
import com.healthcareai.integration.GeminiClient;
import com.healthcareai.repository.ConversationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies the public chat endpoint end-to-end, with the Gemini client
 * mocked so no real API calls are made in tests.
 */
class ChatControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ConversationRepository conversationRepository;

    @MockBean
    private GeminiClient geminiClient;

    @Test
    void chat_isPubliclyAccessible_andPersistsConversationHistory() {
        when(geminiClient.createChatCompletion(any(), any()))
                .thenReturn(new ChatCompletionResult(ChatMessage.assistant("Our clinic is open 9am-5pm, Monday to Friday."), "stop"));
        when(geminiClient.createEmbedding(any())).thenReturn(new float[1536]);

        String sessionId = "session-" + UUID.randomUUID();
        ChatRequest request = new ChatRequest(sessionId, "What are your opening hours?", null, null);

        ResponseEntity<ChatResponse> response = restTemplate.postForEntity("/api/chat", request, ChatResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().reply()).contains("9am-5pm");

        List<Conversation> history = conversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getMessage()).isEqualTo("What are your opening hours?");
        assertThat(history.get(1).getMessage()).contains("9am-5pm");
    }
}
