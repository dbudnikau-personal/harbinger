package com.harbinger.orchestrator;

import com.harbinger.audit.AuditLogger;
import com.harbinger.conversation.ConversationStore;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.Message;
import com.harbinger.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestratorTest {

    private static final Project API_METER = new Project("api-meter", "/path", "desc");
    private static final Project GENERAL = new Project("general", "", "General assistant");
    private static final String CONVERSATION_ID = "test-conv-id";

    @Mock
    private AgentPort projectAgent;

    @Mock
    private FallbackAgent fallbackAgent;

    @Mock
    private LlmRouter router;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private ConversationStore conversationStore;

    @Test
    void shouldRouteToMatchingAgent() {
        String query = "How does api-meter track usage?";
        AgentResponse expected = new AgentResponse("It tracks via REST API", API_METER);

        when(projectAgent.project()).thenReturn(API_METER);
        when(router.route(query, List.of(projectAgent))).thenReturn("api-meter");
        when(conversationStore.get(CONVERSATION_ID)).thenReturn(List.of());
        when(projectAgent.handle(eq(query), anyList())).thenReturn(expected);

        Orchestrator orchestrator = new Orchestrator(
                List.of(projectAgent), fallbackAgent, router, auditLogger, conversationStore);
        AgentResponse result = orchestrator.dispatch(query, CONVERSATION_ID);

        assertEquals(expected, result);
        verify(projectAgent).handle(eq(query), anyList());
        verify(conversationStore).append(eq(CONVERSATION_ID), any(Message.class), any(Message.class));
        verify(auditLogger).log(any());
        verifyNoInteractions(fallbackAgent);
    }

    @Test
    void shouldFallbackWhenRouterReturnsGeneral() {
        String query = "What is the weather today?";
        AgentResponse fallbackResponse = new AgentResponse("I don't know the weather", GENERAL);

        when(projectAgent.project()).thenReturn(API_METER);
        when(router.route(query, List.of(projectAgent))).thenReturn("general");
        when(conversationStore.get(CONVERSATION_ID)).thenReturn(List.of());
        when(fallbackAgent.handle(eq(query), anyList())).thenReturn(fallbackResponse);

        Orchestrator orchestrator = new Orchestrator(
                List.of(projectAgent), fallbackAgent, router, auditLogger, conversationStore);
        AgentResponse result = orchestrator.dispatch(query, CONVERSATION_ID);

        assertEquals(fallbackResponse, result);
        verify(fallbackAgent).handle(eq(query), anyList());
        verify(conversationStore).append(eq(CONVERSATION_ID), any(Message.class), any(Message.class));
    }

    @Test
    void shouldPassHistoryToAgent() {
        String query = "Follow up question";
        List<Message> history = List.of(
                new Message(Message.Role.USER, "Previous question"),
                new Message(Message.Role.ASSISTANT, "Previous answer")
        );
        AgentResponse expected = new AgentResponse("Follow up answer", API_METER);

        when(projectAgent.project()).thenReturn(API_METER);
        when(router.route(query, List.of(projectAgent))).thenReturn("api-meter");
        when(conversationStore.get(CONVERSATION_ID)).thenReturn(history);
        when(projectAgent.handle(query, history)).thenReturn(expected);

        Orchestrator orchestrator = new Orchestrator(
                List.of(projectAgent), fallbackAgent, router, auditLogger, conversationStore);
        AgentResponse result = orchestrator.dispatch(query, CONVERSATION_ID);

        assertEquals(expected, result);
        verify(projectAgent).handle(query, history);
    }
}
