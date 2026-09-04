package com.donotmiss.backend.aiapplication.agent;

import java.util.Map;

public record AgentToolExecutionRequest(
        Long runId,
        Long stepId,
        String userId,
        Map<String, Object> input
) {
}
