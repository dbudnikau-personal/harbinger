package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JuntoAgent extends ProjectAgent {

    private final Project resolvedProject;

    JuntoAgent(
            LlmPort llm,
            ProjectContextLoader contextLoader,
            @Value("${harbinger.projects.junto-app.path}") String path
    ) {
        super(llm, contextLoader);
        this.resolvedProject = new Project(
                "junto-app", path,
                "Spring Cloud microservices app with Keycloak, Kafka, GCP GKE"
        );
    }

    @Override
    public Project project() {
        return resolvedProject;
    }
}
