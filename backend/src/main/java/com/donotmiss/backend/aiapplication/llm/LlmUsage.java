package com.donotmiss.backend.aiapplication.llm;

/** Nullable counts are permitted because OpenAI-compatible providers vary. */
public record LlmUsage(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {
}
