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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.DoctorRequest;
import com.healthcareai.dto.DoctorResponse;
import com.healthcareai.dto.DoctorUpdateRequest;
import com.healthcareai.entity.Doctor;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.mapper.DoctorMapper;
import com.healthcareai.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Doctor management: {@code GET/POST/PUT /api/doctors}.
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

    @PostMapping
    @Operation(summary = "Add a new doctor.")
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = doctorService.createDoctor(
                request.firstName(), request.lastName(), request.specialty(), request.email(),
                request.phoneNumber(), request.bio(), request.workingHoursStart(), request.workingHoursEnd(),
                request.workingDaysCsv());
        return ResponseEntity.ok(doctorMapper.toResponse(doctor));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a doctor's details, status, and/or working schedule.")
    public ResponseEntity<DoctorResponse> update(@PathVariable UUID id, @RequestBody DoctorUpdateRequest request) {
        Doctor doctor = doctorService.updateDoctor(
                id, request.specialty(), request.email(), request.phoneNumber(), request.bio(), request.active(),
                request.workingHoursStart(), request.workingHoursEnd(), request.workingDaysCsv());
        return ResponseEntity.ok(doctorMapper.toResponse(doctor));
    }
}
