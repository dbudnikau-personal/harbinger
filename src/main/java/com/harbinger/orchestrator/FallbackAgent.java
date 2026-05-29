package com.harbinger.orchestrator;

import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Message;
import com.harbinger.domain.Project;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class FallbackAgent implements AgentPort {

    private static final Project GENERAL = new Project("general", "", "General assistant");
    private static final String SYSTEM_PROMPT = "You are a helpful assistant for a software developer.";

    private final LlmPort llm;

    FallbackAgent(LlmPort llm) {
        this.llm = llm;
    }

    @Override
    public AgentResponse handle(String query, List<Message> history) {
        return new AgentResponse(llm.chat(SYSTEM_PROMPT, history, query), GENERAL);
    }

    @Override
    public Project project() {
        return GENERAL;
    }
}
