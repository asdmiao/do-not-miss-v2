package com.donotmiss.backend.aiapplication.agent;

import java.util.Map;

/** A planned, auditable action. Arguments must be sanitized before tracing. */
public record AgentAction(
        String actionId,
        AgentActionType type,
        String name,
        String rationale,
        Map<String, Object> arguments
) {
}
