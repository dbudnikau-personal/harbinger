package com.harbinger.orchestrator;

import com.harbinger.audit.AuditLogger;
import com.harbinger.conversation.ConversationStore;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.Message;
import com.harbinger.domain.Project;
import com.harbinger.domain.SecretLeakException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestratorStreamTest {

    private static final String CONVERSATION_ID = "stream-conversation";
    private static final Project API_METER = new Project("api-meter", "/path", "description");

    @Mock
    private FallbackAgent fallbackAgent;

    @Mock
    private LlmRouter router;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private ConversationStore conversationStore;

    @Test
    void shouldEmitChunksInOrderAndAppendCompleteAnswerAfterSuccess() {
        StreamingAgent agent = new StreamingAgent(List.of("Hello", ", ", "world!"));
        List<String> chunks = new ArrayList<>();
        when(router.route("Tell me", List.of(agent))).thenReturn("api-meter");
        when(conversationStore.get(CONVERSATION_ID)).thenReturn(List.of());

        Orchestrator orchestrator = new Orchestrator(
                List.of(agent), fallbackAgent, router, auditLogger, conversationStore);
        AgentResponse response = orchestrator.dispatchStream("Tell me", CONVERSATION_ID, chunks::add);

        assertEquals(List.of("Hello", ", ", "world!"), chunks);
        assertEquals("Hello, world!", response.content());
        assertEquals(API_METER, response.project());

        ArgumentCaptor<Message> assistantMessage = ArgumentCaptor.forClass(Message.class);
        verify(conversationStore).append(eq(CONVERSATION_ID), any(Message.class), assistantMessage.capture());
        assertEquals(new Message(Message.Role.ASSISTANT, "Hello, world!"), assistantMessage.getValue());
        verify(auditLogger).log(any());
    }

    @Test
    void shouldAbortAndLeaveHistoryUnchangedWhenAccumulatedChunksContainSecret() {
        StreamingAgent agent = new StreamingAgent(List.of("Here is api_key=", "12345678"));
        when(router.route("Tell me", List.of(agent))).thenReturn("api-meter");
        when(conversationStore.get(CONVERSATION_ID)).thenReturn(List.of());

        Orchestrator orchestrator = new Orchestrator(
                List.of(agent), fallbackAgent, router, auditLogger, conversationStore);

        assertThrows(SecretLeakException.class,
                () -> orchestrator.dispatchStream("Tell me", CONVERSATION_ID, chunk -> { }));

        verify(conversationStore, never()).append(any(), any(), any());
        verify(auditLogger, never()).log(any());
    }

    private static final class StreamingAgent implements AgentPort {

        private final List<String> chunks;

        private StreamingAgent(List<String> chunks) {
            this.chunks = chunks;
        }

        @Override
        public AgentResponse handle(String query, List<Message> history) {
            return new AgentResponse(String.join("", chunks), API_METER);
        }

        @Override
        public AgentResponse handleStream(String query, List<Message> history, Consumer<String> onChunk) {
            chunks.forEach(onChunk);
            return new AgentResponse(String.join("", chunks), API_METER);
        }

        @Override
        public Project project() {
            return API_METER;
        }
    }
}
