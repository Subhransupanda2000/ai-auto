package com.healthcareai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the AI Automation Platform for Healthcare Clinics.
 * <p>
 * This backend provides an AI Receptionist capable of answering patient questions,
 * managing appointments, and integrating with external communication channels
 * (WhatsApp, SMS, Email) and calendar systems, backed by a Retrieval Augmented
 * Generation (RAG) knowledge base stored in PostgreSQL/pgvector.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class AiAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAutomationApplication.class, args);
    }
}
