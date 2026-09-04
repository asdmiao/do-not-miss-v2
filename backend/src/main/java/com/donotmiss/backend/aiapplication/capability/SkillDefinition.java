package com.donotmiss.backend.aiapplication.capability;

import java.util.List;

/** Versioned dictionary entry; skillCode is the only cross-domain join key. */
public record SkillDefinition(
        String skillCode,
        String name,
        String category,
        String version,
        List<String> aliases,
        SkillRubric rubric
) {
}
