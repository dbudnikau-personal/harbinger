package com.harbinger.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditLoggerTest {

    private final AuditLogger auditLogger = new AuditLogger();

    @Test
    void shouldLogEventWithoutThrowing() {
        AgentInvocationEvent event = AgentInvocationEvent.of("ApiMeterAgent", "api-meter", 120L);
        assertDoesNotThrow(() -> auditLogger.log(event));
    }

    @Test
    void shouldCaptureTimestampOnCreation() {
        AgentInvocationEvent event = AgentInvocationEvent.of("HermesAgent", "hermes", 45L);
        assertNotNull(event.timestamp());
    }

    @Test
    void shouldPreserveAllFields() {
        AgentInvocationEvent event = AgentInvocationEvent.of("ResumeAgent", "resume", 200L);
        assertNotNull(event.agentName());
        assertNotNull(event.projectName());
        assertNotNull(event.timestamp());
    }
}
