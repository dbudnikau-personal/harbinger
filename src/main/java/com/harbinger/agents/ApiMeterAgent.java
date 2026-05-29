package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ApiMeterAgent extends ProjectAgent {

    private static final Project PROJECT = new Project(
            "api-meter",
            "${harbinger.projects.api-meter.path}",
            "REST API for tracking AI model usage across projects"
    );

    private final Project resolvedProject;

    ApiMeterAgent(
            LlmPort llm,
            ProjectContextLoader contextLoader,
            @Value("${harbinger.projects.api-meter.path}") String path
    ) {
        super(llm, contextLoader);
        this.resolvedProject = new Project("api-meter", path, PROJECT.description());
    }

    @Override
    public boolean supports(String query) {
        String lower = query.toLowerCase();
        return lower.contains("api-meter") || lower.contains("usage") || lower.contains("tracking");
    }

    @Override
    public Project project() {
        return resolvedProject;
    }
}
