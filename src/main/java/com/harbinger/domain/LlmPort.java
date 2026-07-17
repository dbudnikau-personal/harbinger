package com.harbinger.domain;

import java.util.List;
import java.util.function.Consumer;

public interface LlmPort {

    String chat(String systemPrompt, List<Message> history, String userMessage);

    default void chatStream(
            String systemPrompt,
            List<Message> history,
            String userMessage,
            Consumer<String> onChunk
    ) {
        onChunk.accept(chat(systemPrompt, history, userMessage));
    }

    default String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, List.of(), userMessage);
    }
}
