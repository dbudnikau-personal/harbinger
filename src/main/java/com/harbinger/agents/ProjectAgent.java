package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Message;
import com.harbinger.domain.Project;
import java.util.List;

abstract class ProjectAgent implements AgentPort {

    private final LlmPort llm;
    private final ProjectContextLoader contextLoader;

    protected ProjectAgent(LlmPort llm, ProjectContextLoader contextLoader) {
        this.llm = llm;
        this.contextLoader = contextLoader;
    }

    @Override
    public AgentResponse handle(String query, List<Message> history) {
        String context = contextLoader.loadClaudeMd(project()).orElse("");
        String systemPrompt = buildSystemPrompt(context);
        return new AgentResponse(llm.chat(systemPrompt, history, query), project());
    }

    private String buildSystemPrompt(String projectContext) {
        String base = "You are an expert assistant for the " + project().name() + " project. "
                + project().description();
        if (projectContext.isBlank()) {
            return base;
        }
        return base + "\n\nProject context:\n" + projectContext;
    }

    @Override
    public abstract Project project();
}
