package com.donotmiss.backend.aiapplication.agent;

import java.util.Map;

public record AgentToolExecutionResult(
        Map<String, Object> output,
        String outputSummary
) {
}
