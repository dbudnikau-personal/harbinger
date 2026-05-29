package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class HrVacancyAgent extends ProjectAgent {

    private final Project resolvedProject;

    HrVacancyAgent(
            LlmPort llm,
            ProjectContextLoader contextLoader,
            @Value("${harbinger.projects.hr-vacancy-bot-aws.path}") String path
    ) {
        super(llm, contextLoader);
        this.resolvedProject = new Project(
                "hr-vacancy-bot-aws", path,
                "AWS Lambda bot for HR vacancy scraping with Playwright and DeepSeek"
        );
    }

    @Override
    public Project project() {
        return resolvedProject;
    }
}
