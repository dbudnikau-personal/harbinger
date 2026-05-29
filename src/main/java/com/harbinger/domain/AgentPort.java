package com.harbinger.domain;

public interface AgentPort {

    AgentResponse handle(String query);

    Project project();
}
