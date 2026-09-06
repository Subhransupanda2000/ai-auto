package com.healthcareai.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.ai.agent.ReceptionistAgent;
import com.healthcareai.dto.ChatRequest;
import com.healthcareai.dto.ChatResponse;
import com.healthcareai.entity.Channel;
import com.healthcareai.entity.Patient;
import com.healthcareai.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Entry point for the AI receptionist chat: {@code POST /api/chat}.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI receptionist chat endpoint")
public class ChatController {

    private final ReceptionistAgent receptionistAgent;
    private final PatientService patientService;

    @PostMapping
    @Operation(summary = "Send a message to the AI receptionist and receive its reply.")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        Channel channel = request.channel() != null ? request.channel() : Channel.WEB;

        UUID patientId = null;
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            Patient patient = patientService.findOrCreateByPhoneNumber(request.phoneNumber(), null);
            patientId = patient.getId();
        }

        String reply = receptionistAgent.handleUserMessage(request.sessionId(), channel, patientId, request.message());
        return ResponseEntity.ok(new ChatResponse(request.sessionId(), reply));
    }
}
