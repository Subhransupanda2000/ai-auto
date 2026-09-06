package com.healthcareai.ai.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.entity.Patient;
import com.healthcareai.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Exposes patient record management to the AI agent: registering new
 * patients, looking them up (typically by phone number), and updating
 * their details.
 */
@Component
@RequiredArgsConstructor
public class PatientTool implements ToolProvider {

    private final PatientService patientService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToolFunction> getFunctions() {
        return List.of(
                new SimpleToolFunction(createPatientDefinition(), this::createPatient),
                new SimpleToolFunction(findPatientDefinition(), this::findPatient),
                new SimpleToolFunction(updatePatientDefinition(), this::updatePatient));
    }

    private ToolDefinition createPatientDefinition() {
        return new ToolDefinition(
                "patient_createPatient",
                "Registers a new patient record. Use this when a patient who cannot be found by phone number wants "
                        + "to book an appointment or otherwise needs a record created.",
                """
                {
                  "type": "object",
                  "properties": {
                    "firstName": { "type": "string" },
                    "lastName": { "type": "string" },
                    "phoneNumber": { "type": "string", "description": "E.164 phone number, e.g. +15551234567." },
                    "email": { "type": "string" },
                    "dateOfBirth": { "type": "string", "description": "ISO-8601 date, e.g. 1990-05-21." },
                    "gender": { "type": "string" },
                    "notes": { "type": "string" }
                  },
                  "required": ["firstName", "lastName", "phoneNumber"]
                }
                """);
    }

    private ToolDefinition findPatientDefinition() {
        return new ToolDefinition(
                "patient_findPatient",
                "Finds a patient by phone number or by patient id.",
                """
                {
                  "type": "object",
                  "properties": {
                    "phoneNumber": { "type": "string" },
                    "patientId": { "type": "string", "description": "UUID of the patient." }
                  }
                }
                """);
    }

    private ToolDefinition updatePatientDefinition() {
        return new ToolDefinition(
                "patient_updatePatient",
                "Updates fields on an existing patient record.",
                """
                {
                  "type": "object",
                  "properties": {
                    "patientId": { "type": "string" },
                    "firstName": { "type": "string" },
                    "lastName": { "type": "string" },
                    "email": { "type": "string" },
                    "gender": { "type": "string" },
                    "notes": { "type": "string" }
                  },
                  "required": ["patientId"]
                }
                """);
    }

    @SneakyThrows
    private String createPatient(JsonNode args) {
        String firstName = ToolArguments.requiredString(args, "firstName");
        String lastName = ToolArguments.requiredString(args, "lastName");
        String phoneNumber = ToolArguments.requiredString(args, "phoneNumber");
        String email = ToolArguments.optionalString(args, "email");
        String dobText = ToolArguments.optionalString(args, "dateOfBirth");
        LocalDate dateOfBirth = dobText != null ? LocalDate.parse(dobText) : null;
        String gender = ToolArguments.optionalString(args, "gender");
        String notes = ToolArguments.optionalString(args, "notes");

        Patient patient = patientService.createPatient(firstName, lastName, phoneNumber, email, dateOfBirth, gender, notes);
        return objectMapper.writeValueAsString(PatientView.from(patient));
    }

    @SneakyThrows
    private String findPatient(JsonNode args) {
        String phoneNumber = ToolArguments.optionalString(args, "phoneNumber");
        String patientId = ToolArguments.optionalString(args, "patientId");

        Optional<Patient> patient = patientId != null
                ? patientService.findById(java.util.UUID.fromString(patientId))
                : (phoneNumber != null ? patientService.findByPhoneNumber(phoneNumber) : Optional.empty());

        return patient.map(p -> writeQuietly(PatientView.from(p)))
                .orElseGet(() -> writeQuietly(new NotFound("No matching patient was found.")));
    }

    @SneakyThrows
    private String updatePatient(JsonNode args) {
        java.util.UUID patientId = ToolArguments.requiredUuid(args, "patientId");
        String firstName = ToolArguments.optionalString(args, "firstName");
        String lastName = ToolArguments.optionalString(args, "lastName");
        String email = ToolArguments.optionalString(args, "email");
        String gender = ToolArguments.optionalString(args, "gender");
        String notes = ToolArguments.optionalString(args, "notes");

        Patient updated = patientService.updatePatient(patientId, firstName, lastName, email, gender, notes);
        return objectMapper.writeValueAsString(PatientView.from(updated));
    }

    @SneakyThrows
    private String writeQuietly(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private record PatientView(String id, String firstName, String lastName, String phoneNumber, String email) {
        static PatientView from(Patient patient) {
            return new PatientView(patient.getId().toString(), patient.getFirstName(), patient.getLastName(),
                    patient.getPhoneNumber(), patient.getEmail());
        }
    }

    private record NotFound(String message) {
    }
}
