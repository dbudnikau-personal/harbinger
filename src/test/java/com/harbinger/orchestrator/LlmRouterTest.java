package com.harbinger.orchestrator;

import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmRouterTest {

    @Mock
    private LlmPort llm;

    @InjectMocks
    private LlmRouter router;

    private final AgentPort apiMeterAgent = new StubAgent("api-meter", "REST API for tracking usage");
    private final AgentPort hermesAgent = new StubAgent("hermes", "Telegram bot on GCP");

    @Test
    void shouldReturnProjectNameFromLlm() {
        when(llm.chat(anyString(), contains("api-meter"))).thenReturn("api-meter");

        String result = router.route("How does api-meter work?", List.of(apiMeterAgent, hermesAgent));

        assertEquals("api-meter", result);
    }

    @Test
    void shouldReturnGeneralWhenLlmSaysGeneral() {
        when(llm.chat(anyString(), anyString())).thenReturn("general");

        String result = router.route("What is the weather?", List.of(apiMeterAgent, hermesAgent));

        assertEquals("general", result);
    }

    @Test
    void shouldNormalizeResponseToLowerCase() {
        when(llm.chat(anyString(), anyString())).thenReturn("  Hermes  ");

        String result = router.route("Tell me about the bot", List.of(apiMeterAgent, hermesAgent));

        assertEquals("hermes", result);
    }

    @Test
    void shouldIncludeAllProjectsInRoutingPrompt() {
        when(llm.chat(anyString(), contains("hermes"))).thenReturn("hermes");

        String result = router.route("Telegram question", List.of(apiMeterAgent, hermesAgent));

        assertEquals("hermes", result);
    }

    private record StubAgent(String name, String description) implements AgentPort {

        @Override
        public AgentResponse handle(String query) {
            return new AgentResponse("stub", project());
        }

        @Override
        public Project project() {
            return new Project(name, "/path", description);
        }
    }
}
