package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Message;
import com.harbinger.domain.Project;
import java.util.List;
import java.util.function.Consumer;

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

    @Override
    public AgentResponse handleStream(String query, List<Message> history, Consumer<String> onChunk) {
        String context = contextLoader.loadClaudeMd(project()).orElse("");
        String systemPrompt = buildSystemPrompt(context);
        StringBuilder answer = new StringBuilder();
        llm.chatStream(systemPrompt, history, query, chunk -> {
            answer.append(chunk);
            onChunk.accept(chunk);
        });
        return new AgentResponse(answer.toString(), project());
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
