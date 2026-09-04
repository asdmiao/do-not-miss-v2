package com.donotmiss.backend.aiapplication.llm;

public record LlmMessage(Role role, String content) {
    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT,
        TOOL
    }
}
