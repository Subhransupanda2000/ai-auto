package com.healthcareai;

import org.junit.jupiter.api.Test;

/**
 * Verifies the full Spring context (including Flyway migrations against a
 * real PostgreSQL + pgvector database) starts successfully.
 */
class AiAutomationApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Intentionally empty: a failure to start the context fails this test.
    }
}
