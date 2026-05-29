package com.harbinger.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogger.class);

    public void log(AgentInvocationEvent event) {
        LOG.info("agent={} project={} latencyMs={} timestamp={}",
                event.agentName(),
                event.projectName(),
                event.latencyMs(),
                event.timestamp()
        );
    }
}
