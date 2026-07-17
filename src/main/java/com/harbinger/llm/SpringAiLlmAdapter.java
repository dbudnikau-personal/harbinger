package com.harbinger.llm;

import com.harbinger.domain.LlmPort;
import com.harbinger.domain.Message;
import java.util.List;
import java.util.function.Consumer;
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
                .map(m -> sanitizeAndConvert(m))
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

    @Override
    public void chatStream(
            String systemPrompt,
            List<Message> history,
            String userMessage,
            Consumer<String> onChunk
    ) {
        String sanitizedMessage = inputSanitizer.sanitize(userMessage);
        secretsGuard.assertNoSecrets(systemPrompt, sanitizedMessage);

        List<org.springframework.ai.chat.messages.Message> springAiHistory = history.stream()
                .map(this::sanitizeAndConvert)
                .toList();
        StringBuilder answer = new StringBuilder();

        chatClient.prompt()
                .system(systemPrompt)
                .messages(springAiHistory)
                .user(sanitizedMessage)
                .stream()
                .content()
                .doOnNext(chunk -> {
                    answer.append(chunk);
                    secretsGuard.assertNoSecretsInResponse(chunk);
                    secretsGuard.assertNoSecretsInResponse(answer.toString());
                    onChunk.accept(chunk);
                })
                .blockLast();
    }

    private org.springframework.ai.chat.messages.Message sanitizeAndConvert(Message message) {
        String sanitized = inputSanitizer.sanitize(message.content());
        return switch (message.role()) {
            case USER -> new UserMessage(sanitized);
            case ASSISTANT -> new AssistantMessage(sanitized);
            case SYSTEM -> new org.springframework.ai.chat.messages.SystemMessage(sanitized);
        };
    }
}
