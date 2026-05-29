package com.harbinger.domain;

public interface LlmPort {

    String chat(String systemPrompt, String userMessage);
}
