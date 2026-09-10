package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.PatientRequest;
import com.healthcareai.dto.PatientResponse;
import com.healthcareai.dto.PatientUpdateRequest;
import com.healthcareai.entity.Patient;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.mapper.PatientMapper;
import com.healthcareai.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Patient management: {@code GET/POST/PUT /api/patients}.
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

    @PostMapping
    @Operation(summary = "Register a new patient.")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.createPatient(
                request.firstName(), request.lastName(), request.phoneNumber(), request.email(),
                request.dateOfBirth(), request.gender(), null);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a patient's details.")
    public ResponseEntity<PatientResponse> update(@PathVariable UUID id, @RequestBody PatientUpdateRequest request) {
        Patient patient = patientService.updatePatient(
                id, request.firstName(), request.lastName(), request.email(), request.gender(), request.notes());
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }
}
