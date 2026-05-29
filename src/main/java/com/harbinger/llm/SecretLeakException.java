package com.harbinger.llm;

public class SecretLeakException extends RuntimeException {

    public SecretLeakException(String message) {
        super(message);
    }
}
