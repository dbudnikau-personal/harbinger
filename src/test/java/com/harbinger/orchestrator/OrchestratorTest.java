package com.harbinger.orchestrator;

import com.harbinger.audit.AuditLogger;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestratorTest {

    private static final Project API_METER = new Project("api-meter", "/path", "desc");
    private static final Project GENERAL = new Project("general", "", "General assistant");

    @Mock
    private AgentPort projectAgent;

    @Mock
    private FallbackAgent fallbackAgent;

    @Mock
    private LlmRouter router;

    @Mock
    private AuditLogger auditLogger;

    @Test
    void shouldRouteToMatchingAgent() {
        String query = "How does api-meter track usage?";
        AgentResponse expected = new AgentResponse("It tracks via REST API", API_METER);

        when(projectAgent.project()).thenReturn(API_METER);
        when(router.route(query, List.of(projectAgent))).thenReturn("api-meter");
        when(projectAgent.handle(query)).thenReturn(expected);

        Orchestrator orchestrator = new Orchestrator(List.of(projectAgent), fallbackAgent, router, auditLogger);
        AgentResponse result = orchestrator.dispatch(query);

        assertEquals(expected, result);
        verify(projectAgent).handle(query);
        verify(auditLogger).log(any());
        verifyNoInteractions(fallbackAgent);
    }

    @Test
    void shouldFallbackWhenRouterReturnsGeneral() {
        String query = "What is the weather today?";
        AgentResponse fallbackResponse = new AgentResponse("I don't know the weather", GENERAL);

        when(projectAgent.project()).thenReturn(API_METER);
        when(router.route(query, List.of(projectAgent))).thenReturn("general");
        when(fallbackAgent.handle(query)).thenReturn(fallbackResponse);

        Orchestrator orchestrator = new Orchestrator(List.of(projectAgent), fallbackAgent, router, auditLogger);
        AgentResponse result = orchestrator.dispatch(query);

        assertEquals(fallbackResponse, result);
        verify(fallbackAgent).handle(query);
        verify(auditLogger).log(any());
    }

    @Test
    void shouldFallbackWhenRouterReturnsUnknownProject() {
        String query = "Something completely unrelated";
        AgentResponse fallbackResponse = new AgentResponse("Here is a general answer", GENERAL);

        when(projectAgent.project()).thenReturn(API_METER);
        when(router.route(query, List.of(projectAgent))).thenReturn("unknown-project");
        when(fallbackAgent.handle(query)).thenReturn(fallbackResponse);

        Orchestrator orchestrator = new Orchestrator(List.of(projectAgent), fallbackAgent, router, auditLogger);
        AgentResponse result = orchestrator.dispatch(query);

        assertEquals(fallbackResponse, result);
        verify(fallbackAgent).handle(query);
        verify(auditLogger).log(any());
    }
}
