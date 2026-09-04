package com.donotmiss.backend.aiapplication.async;

import java.time.Instant;

/**
 * idempotencyKey is deterministic: taskType + source stable id + content hash
 * + contract/version. Consumers must acquire it before executing side effects.
 */
public record AsyncTaskCommand(
        AsyncTaskType taskType,
        String idempotencyKey,
        String aggregateType,
        String aggregateId,
        String contentHash,
        String contractVersion,
        Instant occurredAt
) {
}
