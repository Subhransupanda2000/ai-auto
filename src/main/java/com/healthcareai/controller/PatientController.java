package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.PatientResponse;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.mapper.PatientMapper;
import com.healthcareai.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Patient lookups: {@code GET /api/patients}.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @GetMapping
    @Operation(summary = "List all patients.")
    public ResponseEntity<List<PatientResponse>> list() {
        return ResponseEntity.ok(patientService.findAll().stream().map(patientMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a patient by id.")
    public ResponseEntity<PatientResponse> get(@PathVariable UUID id) {
        return patientService.findById(id)
                .map(patientMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", id));
    }
}
