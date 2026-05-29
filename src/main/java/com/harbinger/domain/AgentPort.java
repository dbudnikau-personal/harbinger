package com.harbinger.domain;

import java.util.List;

public interface AgentPort {

    AgentResponse handle(String query, List<Message> history);

    Project project();
}
