package com.donotmiss.backend.aiapplication.agent;

import java.time.Instant;
import java.util.Map;

/** Sanitized result of an action or tool call that may drive the next state. */
public record AgentObservation(
        String observationId,
        String source,
        String summary,
        Map<String, Object> data,
        Instant observedAt
) {
}
