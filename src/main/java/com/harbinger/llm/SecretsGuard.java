package com.harbinger.llm;

import com.harbinger.domain.SecretLeakException;
import com.harbinger.domain.SecretPatternMatcher;
import org.springframework.stereotype.Component;

@Component
class SecretsGuard {

    void assertNoSecrets(String systemPrompt, String userMessage) {
        assertClean("systemPrompt", systemPrompt);
        assertClean("userMessage", userMessage);
    }

    void assertNoSecretsInResponse(String response) {
        assertClean("llmResponse", response);
    }

    void assertNoSecretsInContext(String context) {
        assertClean("projectContext", context);
    }

    private void assertClean(String field, String value) {
        if (SecretPatternMatcher.containsSecret(value)) {
            throw new SecretLeakException(
                    "Secret-like pattern detected in field '" + field + "' — aborting LLM call"
            );
        }
    }
}
