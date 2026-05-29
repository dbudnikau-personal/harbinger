package com.harbinger.domain;

public class SecretLeakException extends RuntimeException {

    public SecretLeakException(String message) {
        super(message);
    }
}
