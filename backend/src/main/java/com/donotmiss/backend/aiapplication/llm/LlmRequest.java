package com.donotmiss.backend.aiapplication.llm;

import java.util.List;
import java.util.Map;

/** Provider-neutral request. Provider adapters map this to their wire format. */
public record LlmRequest(
        String model,
        List<LlmMessage> messages,
        Double temperature,
        Integer maxTokens,
        Long timeoutMillis,
        String requestId,
        String promptVersion,
        ResponseFormat responseFormat,
        Map<String, Object> metadata
) {
    public enum ResponseFormat {
        TEXT,
        JSON_OBJECT
    }
}
