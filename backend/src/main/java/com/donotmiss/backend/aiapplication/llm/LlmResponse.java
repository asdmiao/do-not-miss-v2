package com.donotmiss.backend.aiapplication.llm;

import java.util.Map;

public record LlmResponse(
        String content,
        String model,
        LlmUsage usage,
        String finishReason,
        long latencyMillis,
        String requestId,
        Map<String, Object> providerMetadata
) {
}
