package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class HermesAgent extends ProjectAgent {

    private final Project resolvedProject;

    HermesAgent(
            LlmPort llm,
            ProjectContextLoader contextLoader,
            @Value("${harbinger.projects.hermes.path}") String path
    ) {
        super(llm, contextLoader);
        this.resolvedProject = new Project("hermes", path, "Telegram bot powered by Nous Hermes on GCP");
    }

    @Override
    public boolean supports(String query) {
        String lower = query.toLowerCase();
        return lower.contains("hermes") || lower.contains("telegram") || lower.contains("bot");
    }

    @Override
    public Project project() {
        return resolvedProject;
    }
}
