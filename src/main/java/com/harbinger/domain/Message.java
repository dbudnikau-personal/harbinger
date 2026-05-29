package com.harbinger.domain;

public record Message(Role role, String content) {

    public enum Role {
        USER, ASSISTANT, SYSTEM
    }
}
