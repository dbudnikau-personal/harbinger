package com.harbinger.orchestrator;

import com.harbinger.domain.AgentPort;
import com.harbinger.domain.LlmPort;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class LlmRouter {

    private static final String SYSTEM_PROMPT = """
            You are a routing assistant. Your only job is to identify which project a user query is about.
            Reply with ONLY the project name from the provided list, or "general" if the query doesn't relate to any project.
            Do not explain. Do not add punctuation. Output a single word.
            """;

    private final LlmPort llm;

    LlmRouter(LlmPort llm) {
        this.llm = llm;
    }

    String route(String query, List<AgentPort> agents) {
        String projectList = agents.stream()
                .map(a -> "- " + a.project().name() + ": " + a.project().description())
                .collect(Collectors.joining("\n"));

        String userMessage = "Projects:\n" + projectList + "\n\nQuery: " + query;
        return llm.chat(SYSTEM_PROMPT, userMessage).strip().toLowerCase();
    }
}
