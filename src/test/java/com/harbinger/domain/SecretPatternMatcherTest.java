package com.harbinger.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretPatternMatcherTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "sk-abcdefghijklmnopqrstuvwxyz123456789012",
        "sk-ant-api03-someSecretKeyValueHere12345678901234567890",
        "AKIAIOSFODNN7EXAMPLE",
        "password=supersecret123",
        "api_key=verysecretvalue",
        "-----BEGIN RSA PRIVATE KEY-----",
        "-----BEGIN OPENSSH PRIVATE KEY-----",
        "jdbc:postgresql://localhost:5432/mydb?user=admin&password=secret",
        "mongodb://user:pass@localhost:27017/mydb",
        "redis://default:password@localhost:6379",
        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIn0.signature",
        "AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
    })
    void shouldDetectSecretPatterns(String secret) {
        assertTrue(SecretPatternMatcher.containsSecret(secret));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "How do I add a new endpoint?",
        "The project uses Spring Boot 3.5.14",
        "Run ./build.sh to compile",
        "See README.md for details"
    })
    void shouldNotFlagNormalContent(String content) {
        assertFalse(SecretPatternMatcher.containsSecret(content));
    }

    @Test
    void shouldReturnFalseForNull() {
        assertFalse(SecretPatternMatcher.containsSecret(null));
    }

    @Test
    void shouldReturnFalseForBlank() {
        assertFalse(SecretPatternMatcher.containsSecret("   "));
    }
}
