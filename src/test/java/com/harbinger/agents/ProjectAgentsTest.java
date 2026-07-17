package com.harbinger.agents;

import com.harbinger.context.ProjectContextLoader;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Message;
import com.harbinger.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAgentsTest {

    private static final String API_METER_PATH = "/projects/api-meter";
    private static final String HERMES_PATH = "/projects/hermes";
    private static final String JUNTO_PATH = "/projects/junto-app";
    private static final String HR_VACANCY_PATH = "/projects/hr-vacancy-bot-aws";
    private static final String RESUME_PATH = "/projects/resume";

    @Mock
    private LlmPort llm;

    @Mock
    private ProjectContextLoader contextLoader;

    record AgentCase(String expectedName, String path, Function<AgentFactory, ProjectAgent> factory) {
    }

    record AgentFactory(LlmPort llm, ProjectContextLoader contextLoader) {
    }

    static Stream<AgentCase> allAgents() {
        return Stream.of(
                new AgentCase("api-meter", API_METER_PATH,
                        f -> new ApiMeterAgent(f.llm(), f.contextLoader(), API_METER_PATH)),
                new AgentCase("hermes", HERMES_PATH,
                        f -> new HermesAgent(f.llm(), f.contextLoader(), HERMES_PATH)),
                new AgentCase("junto-app", JUNTO_PATH,
                        f -> new JuntoAgent(f.llm(), f.contextLoader(), JUNTO_PATH)),
                new AgentCase("hr-vacancy-bot-aws", HR_VACANCY_PATH,
                        f -> new HrVacancyAgent(f.llm(), f.contextLoader(), HR_VACANCY_PATH)),
                new AgentCase("resume", RESUME_PATH,
                        f -> new ResumeAgent(f.llm(), f.contextLoader(), RESUME_PATH))
        );
    }

    @ParameterizedTest
    @MethodSource("allAgents")
    void shouldExposeProjectMetadata(AgentCase agentCase) {
        ProjectAgent agent = agentCase.factory().apply(new AgentFactory(llm, contextLoader));
        Project project = agent.project();

        assertEquals(agentCase.expectedName(), project.name());
        assertEquals(agentCase.path(), project.path());
        assertFalse(project.description().isBlank());
    }

    @Test
    void handleShouldIncludeLoadedContextInSystemPromptAndPassHistory() {
        ApiMeterAgent agent = new ApiMeterAgent(llm, contextLoader, API_METER_PATH);
        Project project = agent.project();
        String query = "How does api-meter track usage?";
        List<Message> history = List.of(
                new Message(Message.Role.USER, "Previous question"),
                new Message(Message.Role.ASSISTANT, "Previous answer")
        );
        String contextText = "Tracks usage via REST endpoints.";
        String llmAnswer = "It tracks via REST API";

        when(contextLoader.loadClaudeMd(project)).thenReturn(Optional.of(contextText));
        when(llm.chat(any(), eq(history), eq(query))).thenReturn(llmAnswer);

        AgentResponse response = agent.handle(query, history);

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(llm).chat(systemPromptCaptor.capture(), eq(history), eq(query));

        String systemPrompt = systemPromptCaptor.getValue();
        assertTrue(systemPrompt.contains("api-meter"));
        assertTrue(systemPrompt.contains("Project context:"));
        assertTrue(systemPrompt.contains(contextText));
        assertEquals(llmAnswer, response.content());
        assertEquals(project, response.project());
    }

    @Test
    void handleShouldOmitProjectContextBlockWhenLoaderReturnsEmpty() {
        ApiMeterAgent agent = new ApiMeterAgent(llm, contextLoader, API_METER_PATH);
        Project project = agent.project();
        String query = "General question";
        List<Message> history = List.of();

        when(contextLoader.loadClaudeMd(project)).thenReturn(Optional.empty());
        when(llm.chat(any(), eq(history), eq(query))).thenReturn("Answer without context");

        agent.handle(query, history);

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(llm).chat(systemPromptCaptor.capture(), eq(history), eq(query));
        assertFalse(systemPromptCaptor.getValue().contains("Project context:"));
    }

    @Test
    void handleShouldReturnLlmAnswerAndProjectInResponse() {
        ApiMeterAgent agent = new ApiMeterAgent(llm, contextLoader, API_METER_PATH);
        Project project = agent.project();
        String query = "Status?";
        String llmAnswer = "All systems operational";

        when(contextLoader.loadClaudeMd(project)).thenReturn(Optional.empty());
        when(llm.chat(any(), eq(List.of()), eq(query))).thenReturn(llmAnswer);

        AgentResponse response = agent.handle(query, List.of());

        assertEquals(llmAnswer, response.content());
        assertEquals(project, response.project());
    }
}
