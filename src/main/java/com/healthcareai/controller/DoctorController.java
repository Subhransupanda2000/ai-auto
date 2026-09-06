package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.DoctorResponse;
import com.healthcareai.entity.Doctor;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.mapper.DoctorMapper;
import com.healthcareai.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Doctor lookups: {@code GET /api/doctors}.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    @GetMapping
    @Operation(summary = "List active doctors, optionally filtered by specialty.")
    public ResponseEntity<List<DoctorResponse>> list(@RequestParam(required = false) String specialty) {
        List<Doctor> doctors = specialty != null ? doctorService.findBySpecialty(specialty) : doctorService.findActiveDoctors();
        return ResponseEntity.ok(doctors.stream().map(doctorMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a doctor by id.")
    public ResponseEntity<DoctorResponse> get(@PathVariable UUID id) {
        return doctorService.findById(id)
                .map(doctorMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> ResourceNotFoundException.of("Doctor", id));
    }
}
