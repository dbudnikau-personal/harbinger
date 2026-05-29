package com.harbinger.orchestrator;

import com.harbinger.audit.AgentInvocationEvent;
import com.harbinger.audit.AuditLogger;
import com.harbinger.conversation.ConversationStore;
import com.harbinger.domain.AgentPort;
import com.harbinger.domain.AgentResponse;
import com.harbinger.domain.Message;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class Orchestrator {

    private final List<AgentPort> agents;
    private final FallbackAgent fallbackAgent;
    private final LlmRouter router;
    private final AuditLogger auditLogger;
    private final ConversationStore conversationStore;

    Orchestrator(
            List<AgentPort> agents,
            FallbackAgent fallbackAgent,
            LlmRouter router,
            AuditLogger auditLogger,
            ConversationStore conversationStore
    ) {
        this.agents = agents;
        this.fallbackAgent = fallbackAgent;
        this.router = router;
        this.auditLogger = auditLogger;
        this.conversationStore = conversationStore;
    }

    public AgentResponse dispatch(String query, String conversationId) {
        String projectName = router.route(query, agents);
        AgentPort agent = agents.stream()
                .filter(a -> a.project().name().equals(projectName))
                .findFirst()
                .orElse(fallbackAgent);

        List<Message> history = conversationStore.get(conversationId);

        long start = System.currentTimeMillis();
        AgentResponse response = agent.handle(query, history);
        long latencyMs = System.currentTimeMillis() - start;

        conversationStore.append(
                conversationId,
                new Message(Message.Role.USER, query),
                new Message(Message.Role.ASSISTANT, response.content())
        );

        auditLogger.log(AgentInvocationEvent.of(
                agent.getClass().getSimpleName(),
                response.project().name(),
                latencyMs
        ));

        return response;
    }
}
