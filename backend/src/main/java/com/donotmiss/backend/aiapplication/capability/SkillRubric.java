package com.donotmiss.backend.aiapplication.capability;

import java.util.List;

public record SkillRubric(
        String rubricVersion,
        List<String> evidenceCriteria,
        List<String> interviewCriteria,
        List<String> levelDescriptors
) {
}
