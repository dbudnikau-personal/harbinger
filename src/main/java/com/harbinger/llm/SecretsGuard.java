package com.harbinger.llm;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
class SecretsGuard {

    private static final List<Pattern> SECRET_PATTERNS = List.of(
        Pattern.compile("sk-[A-Za-z0-9]{32,}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("sk-ant-[A-Za-z0-9\\-_]{32,}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("AKIA[0-9A-Z]{16}"),
        Pattern.compile("(?i)(password|passwd|secret|api[_-]?key|token)\\s*[:=]\\s*\\S{8,}"),
        Pattern.compile("-----BEGIN (RSA |EC )?PRIVATE KEY-----")
    );

    void assertNoSecrets(String systemPrompt, String userMessage) {
        assertClean("systemPrompt", systemPrompt);
        assertClean("userMessage", userMessage);
    }

    void assertNoSecretsInResponse(String response) {
        assertClean("llmResponse", response);
    }

    private void assertClean(String field, String value) {
        if (value == null) {
            return;
        }
        for (Pattern pattern : SECRET_PATTERNS) {
            if (pattern.matcher(value).find()) {
                throw new SecretLeakException(
                    "Secret-like pattern detected in field '" + field + "' — aborting LLM call"
                );
            }
        }
    }
}
