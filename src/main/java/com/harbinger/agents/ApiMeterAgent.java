package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ApiMeterAgent extends ProjectAgent {

    private final Project resolvedProject;

    ApiMeterAgent(
            LlmPort llm,
            ProjectContextLoader contextLoader,
            @Value("${harbinger.projects.api-meter.path}") String path
    ) {
        super(llm, contextLoader);
        this.resolvedProject = new Project(
                "api-meter", path, "REST API for tracking AI model usage across projects"
        );
    }

    @Override
    public Project project() {
        return resolvedProject;
    }
}
