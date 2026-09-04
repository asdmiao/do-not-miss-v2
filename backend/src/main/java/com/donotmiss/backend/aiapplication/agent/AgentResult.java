package com.donotmiss.backend.aiapplication.agent;

import java.util.List;
import java.util.Map;

/** Result of exactly one decision cycle; applications decide whether to loop. */
public record AgentResult(
        AgentState previousState,
        AgentState nextState,
        AgentAction action,
        List<AgentObservation> observations,
        Map<String, Object> output,
        String completionReason
) {
}
