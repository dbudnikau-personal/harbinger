package com.harbinger.orchestrator;

import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestratorTest {

    private static final Project API_METER = new Project("api-meter", "/path", "desc");
    private static final Project GENERAL = new Project("general", "", "General assistant");

    @Mock
    private AgentPort matchingAgent;

    @Mock
    private AgentPort nonMatchingAgent;

    @Mock
    private FallbackAgent fallbackAgent;

    @Test
    void shouldRouteToMatchingAgent() {
        String query = "How does api-meter track usage?";
        AgentResponse expected = new AgentResponse("It tracks via REST API", API_METER);

        when(matchingAgent.supports(query)).thenReturn(true);
        when(matchingAgent.handle(query)).thenReturn(expected);

        Orchestrator orchestrator = new Orchestrator(List.of(matchingAgent), fallbackAgent);
        AgentResponse result = orchestrator.dispatch(query);

        assertEquals(expected, result);
        verify(matchingAgent).handle(query);
        verifyNoInteractions(fallbackAgent);
    }

    @Test
    void shouldSkipNonMatchingAgentAndRouteToMatching() {
        String query = "Tell me about hermes bot";
        AgentResponse expected = new AgentResponse("Hermes is a Telegram bot", API_METER);

        when(nonMatchingAgent.supports(query)).thenReturn(false);
        when(matchingAgent.supports(query)).thenReturn(true);
        when(matchingAgent.handle(query)).thenReturn(expected);

        Orchestrator orchestrator = new Orchestrator(List.of(nonMatchingAgent, matchingAgent), fallbackAgent);
        AgentResponse result = orchestrator.dispatch(query);

        assertEquals(expected, result);
        verifyNoInteractions(fallbackAgent);
    }

    @Test
    void shouldFallbackWhenNoAgentMatches() {
        String query = "What is the weather today?";
        AgentResponse fallbackResponse = new AgentResponse("I don't know the weather", GENERAL);

        when(nonMatchingAgent.supports(query)).thenReturn(false);
        when(fallbackAgent.handle(query)).thenReturn(fallbackResponse);

        Orchestrator orchestrator = new Orchestrator(List.of(nonMatchingAgent), fallbackAgent);
        AgentResponse result = orchestrator.dispatch(query);

        assertEquals(fallbackResponse, result);
        verify(fallbackAgent).handle(query);
    }
}
