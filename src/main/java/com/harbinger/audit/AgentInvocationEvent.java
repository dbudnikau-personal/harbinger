package com.harbinger.audit;

import java.time.Instant;

public record AgentInvocationEvent(
        String agentName,
        String projectName,
        long latencyMs,
        Instant timestamp
) {

    public static AgentInvocationEvent of(String agentName, String projectName, long latencyMs) {
        return new AgentInvocationEvent(agentName, projectName, latencyMs, Instant.now());
    }
}
