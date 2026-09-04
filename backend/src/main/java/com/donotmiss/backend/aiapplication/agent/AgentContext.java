package com.donotmiss.backend.aiapplication.agent;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Immutable input for one agent decision. The intended transition is
 * state -> action -> tool invocation -> observation -> next state.
 */
public record AgentContext(
        Long runId,
        Long stepId,
        String userId,
        String agentName,
        String goal,
        AgentState state,
        Map<String, Object> inputs,
        List<AgentObservation> observations,
        Instant createdAt
) {
}
