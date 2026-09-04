package com.donotmiss.backend.aiapplication.career.job;

import java.util.List;

/** Strict extraction contract; sourcePosition points to a deterministic JD chunk, not raw LLM provenance. */
public record StructuredJobDescription(List<Requirement> requirements) {
    public record Requirement(String skill, Integer importance, String requiredLevel, String requirementText, Integer sourcePosition) { }
}
