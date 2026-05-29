package com.harbinger.llm;

import com.harbinger.domain.SecretLeakException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecretsGuardTest {

    private final SecretsGuard guard = new SecretsGuard();

    @ParameterizedTest
    @ValueSource(strings = {
        "sk-abcdefghijklmnopqrstuvwxyz123456789012",
        "sk-ant-api03-someSecretKeyValueHere12345678901234567890",
        "AKIAIOSFODNN7EXAMPLE",
        "password=supersecret123",
        "api_key=verysecretvalue",
        "-----BEGIN RSA PRIVATE KEY-----"
    })
    void shouldThrowWhenSecretPatternDetectedInSystemPrompt(String secret) {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecrets(secret, "normal user message"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "sk-abcdefghijklmnopqrstuvwxyz123456789012",
        "AKIAIOSFODNN7EXAMPLE",
        "token=mysecrettoken123"
    })
    void shouldThrowWhenSecretPatternDetectedInUserMessage(String secret) {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecrets("normal system prompt", secret));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "sk-abcdefghijklmnopqrstuvwxyz123456789012",
        "AKIAIOSFODNN7EXAMPLE"
    })
    void shouldThrowWhenSecretPatternDetectedInResponse(String secret) {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecretsInResponse(secret));
    }

    @Test
    void shouldPassCleanContent() {
        assertDoesNotThrow(() -> guard.assertNoSecrets(
                "You are an expert Java developer.",
                "How do I implement a REST endpoint?"
        ));
    }

    @Test
    void shouldPassNullValues() {
        assertDoesNotThrow(() -> guard.assertNoSecrets(null, null));
    }

    @Test
    void shouldPassCleanResponse() {
        assertDoesNotThrow(() -> guard.assertNoSecretsInResponse(
                "Use @RestController and @GetMapping annotations."
        ));
    }
}
