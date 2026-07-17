package com.harbinger.domain;

import java.util.List;
import java.util.function.Consumer;

public interface AgentPort {

    AgentResponse handle(String query, List<Message> history);

    default AgentResponse handleStream(String query, List<Message> history, Consumer<String> onChunk) {
        AgentResponse response = handle(query, history);
        onChunk.accept(response.content());
        return response;
    }

    Project project();
}
