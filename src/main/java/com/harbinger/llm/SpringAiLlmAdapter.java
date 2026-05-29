package com.harbinger.llm;

import com.harbinger.domain.LlmPort;
import org.springframework.ai.chat.client.ChatClient;
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
    public String chat(String systemPrompt, String userMessage) {
        String sanitizedMessage = inputSanitizer.sanitize(userMessage);
        secretsGuard.assertNoSecrets(systemPrompt, sanitizedMessage);

        String response = chatClient.prompt()
            .system(systemPrompt)
            .user(sanitizedMessage)
            .call()
            .content();

        secretsGuard.assertNoSecretsInResponse(response);
        return response;
    }
}
