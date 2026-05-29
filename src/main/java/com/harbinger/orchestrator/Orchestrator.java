package com.harbinger.orchestrator;

import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class Orchestrator {

    private final List<AgentPort> agents;
    private final FallbackAgent fallbackAgent;

    Orchestrator(List<AgentPort> agents, FallbackAgent fallbackAgent) {
        this.agents = agents;
        this.fallbackAgent = fallbackAgent;
    }

    public AgentResponse dispatch(String query) {
        return agents.stream()
            .filter(agent -> agent.supports(query))
            .findFirst()
            .orElse(fallbackAgent)
            .handle(query);
    }
}
