package com.harbinger.domain;

import java.util.List;
import java.util.regex.Pattern;

public final class SecretPatternMatcher {

    private static final List<Pattern> PATTERNS = List.of(
            // API keys
            Pattern.compile("sk-[A-Za-z0-9]{32,}", Pattern.CASE_INSENSITIVE),
            Pattern.compile("sk-ant-[A-Za-z0-9\\-_]{32,}", Pattern.CASE_INSENSITIVE),
            // AWS credentials
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            Pattern.compile("AWS_SECRET_ACCESS_KEY\\s*[:=]\\s*\\S{8,}", Pattern.CASE_INSENSITIVE),
            // Generic secrets
            Pattern.compile("(?i)(password|passwd|secret|api[_-]?key|token|auth[_-]?token)\\s*[:=]\\s*['\"]?\\S{8,}"),
            // Private keys
            Pattern.compile("-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----"),
            // Connection strings
            Pattern.compile("(jdbc|mongodb|redis|amqp|postgresql)://[^\\s]+", Pattern.CASE_INSENSITIVE),
            // Bearer tokens
            Pattern.compile("Bearer\\s+[A-Za-z0-9\\-._~+/]{20,}", Pattern.CASE_INSENSITIVE),
            // SSH private key header
            Pattern.compile("-----BEGIN OPENSSH PRIVATE KEY-----")
    );

    private SecretPatternMatcher() {
    }

    public static boolean containsSecret(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return PATTERNS.stream().anyMatch(p -> p.matcher(value).find());
    }
}
