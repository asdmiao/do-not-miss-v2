package com.donotmiss.backend.aiapplication.agent;

import java.time.Instant;

/** Persistence-ready audit contract. Never place raw user content here. */
public record AgentToolInvocation(
        Long runId,
        Long stepId,
        String toolName,
        String inputHash,
        String sanitizedInput,
        String outputSummary,
        AgentToolInvocationStatus status,
        long latencyMillis,
        String error,
        Instant createdAt
) {
}
