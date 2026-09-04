package com.donotmiss.backend.aiapplication.async;

/** Persistence contract for RabbitMQ consumers; implementations must be atomic. */
public interface AsyncTaskIdempotency {
    boolean acquire(AsyncTaskCommand command);

    void markSucceeded(String idempotencyKey);

    void markFailed(String idempotencyKey, String error);
}
