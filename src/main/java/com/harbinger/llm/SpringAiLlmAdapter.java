package com.harbinger.llm;

import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Message;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

@Component
class SpringAiLlmAdapter implements LlmPort {

    private final ChatClient chatClient;
    private final SecretsGuard secretsGuard;
    private final InputSanitizer inputSanitizer;

    SpringAiLlmAdapter(ChatClient.Builder builder, SecretsGuard secretsGuard, InputSanitizer inputSanitizer) {
        this.chatClient = builder.build();
        this.secretsGuard = secretsGuard;
        this.inputSanitizer = inputSanitizer;
    }

    @Override
    public String chat(String systemPrompt, List<Message> history, String userMessage) {
        String sanitizedMessage = inputSanitizer.sanitize(userMessage);
        secretsGuard.assertNoSecrets(systemPrompt, sanitizedMessage);

        List<org.springframework.ai.chat.messages.Message> springAiHistory = history.stream()
                .map(this::toSpringAiMessage)
                .toList();

        String response = chatClient.prompt()
                .system(systemPrompt)
                .messages(springAiHistory)
                .user(sanitizedMessage)
                .call()
                .content();

        secretsGuard.assertNoSecretsInResponse(response);
        return response;
    }

    private org.springframework.ai.chat.messages.Message toSpringAiMessage(Message message) {
        return switch (message.role()) {
            case USER -> new UserMessage(message.content());
            case ASSISTANT -> new AssistantMessage(message.content());
            case SYSTEM -> new org.springframework.ai.chat.messages.SystemMessage(message.content());
        };
    }
}
