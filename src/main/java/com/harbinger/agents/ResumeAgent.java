package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ResumeAgent extends ProjectAgent {

    private final Project resolvedProject;

    ResumeAgent(
            LlmPort llm,
            ProjectContextLoader contextLoader,
            @Value("${harbinger.projects.resume.path}") String path
    ) {
        super(llm, contextLoader);
        this.resolvedProject = new Project(
                "resume", path,
                "Senior Java/Backend Developer resume — Markdown source, US and EU variants, built with Pandoc and weasyprint"
        );
    }

    @Override
    public Project project() {
        return resolvedProject;
    }
}
