package com.harbinger.llm;

import com.harbinger.domain.SecretLeakException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecretsGuardTest {

    private final SecretsGuard guard = new SecretsGuard();

    @Test
    void shouldThrowWhenSecretInSystemPrompt() {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecrets("api_key=supersecretvalue", "normal message"));
    }

    @Test
    void shouldThrowWhenSecretInUserMessage() {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecrets("normal prompt", "AKIAIOSFODNN7EXAMPLE"));
    }

    @Test
    void shouldThrowWhenSecretInResponse() {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecretsInResponse("sk-abcdefghijklmnopqrstuvwxyz123456789012"));
    }

    @Test
    void shouldThrowWhenSecretInContext() {
        assertThrows(SecretLeakException.class,
                () -> guard.assertNoSecretsInContext("jdbc:postgresql://localhost:5432/db?password=secret"));
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
    void shouldPassCleanContext() {
        assertDoesNotThrow(() -> guard.assertNoSecretsInContext(
                "# Project\nRun ./build.sh to compile. See README for details."
        ));
    }
}
