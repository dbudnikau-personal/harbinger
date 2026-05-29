package com.harbinger.orchestrator;

import com.harbinger.audit.AgentInvocationEvent;
import com.harbinger.audit.AuditLogger;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class Orchestrator {

    private final List<AgentPort> agents;
    private final FallbackAgent fallbackAgent;
    private final LlmRouter router;
    private final AuditLogger auditLogger;

    Orchestrator(List<AgentPort> agents, FallbackAgent fallbackAgent, LlmRouter router, AuditLogger auditLogger) {
        this.agents = agents;
        this.fallbackAgent = fallbackAgent;
        this.router = router;
        this.auditLogger = auditLogger;
    }

    public AgentResponse dispatch(String query) {
        String projectName = router.route(query, agents);
        AgentPort agent = agents.stream()
                .filter(a -> a.project().name().equals(projectName))
                .findFirst()
                .orElse(fallbackAgent);

        long start = System.currentTimeMillis();
        AgentResponse response = agent.handle(query);
        long latencyMs = System.currentTimeMillis() - start;

        auditLogger.log(AgentInvocationEvent.of(
                agent.getClass().getSimpleName(),
                response.project().name(),
                latencyMs
        ));

        return response;
    }
}
