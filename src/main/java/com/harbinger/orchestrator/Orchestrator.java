package com.harbinger.orchestrator;

import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class Orchestrator {

    private final List<AgentPort> agents;
    private final FallbackAgent fallbackAgent;
    private final LlmRouter router;

    Orchestrator(List<AgentPort> agents, FallbackAgent fallbackAgent, LlmRouter router) {
        this.agents = agents;
        this.fallbackAgent = fallbackAgent;
        this.router = router;
    }

    public AgentResponse dispatch(String query) {
        String projectName = router.route(query, agents);
        return agents.stream()
                .filter(agent -> agent.project().name().equals(projectName))
                .findFirst()
                .orElse(fallbackAgent)
                .handle(query);
    }
}
