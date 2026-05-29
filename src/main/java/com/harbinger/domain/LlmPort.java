package com.harbinger.domain;

import java.util.List;

public interface LlmPort {

    String chat(String systemPrompt, List<Message> history, String userMessage);

    default String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, List.of(), userMessage);
    }
}
