package com.harbinger.domain;

public interface AgentPort {

    boolean supports(String query);

    AgentResponse handle(String query);

    Project project();
}
