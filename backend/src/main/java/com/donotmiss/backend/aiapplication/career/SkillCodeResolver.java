package com.donotmiss.backend.aiapplication.career;

import com.donotmiss.backend.aiapplication.capability.SkillDictionary;
import org.springframework.stereotype.Service;

/** Resolves only dictionary-owned codes; extraction text can never create a new code. */
@Service
public class SkillCodeResolver {
    private static final String VERSION = "v1";
    private static final String UNMAPPED = "UNMAPPED";
    private static final String UNKNOWN = "UNKNOWN";

    private final SkillDictionary dictionary;

    public SkillCodeResolver(SkillDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public String resolveOrUnmapped(String extractedSkill) {
        return dictionary.resolveAlias(extractedSkill, VERSION)
                .map(definition -> definition.skillCode())
                .orElse(UNMAPPED);
    }

    /** UNKNOWN means no skill can be determined; UNMAPPED means a named skill is outside the dictionary. */
    public String resolveRequirementSkill(String extractedSkill) {
        if (extractedSkill == null || extractedSkill.isBlank() || "UNKNOWN".equalsIgnoreCase(extractedSkill.trim())) {
            return UNKNOWN;
        }
        return resolveOrUnmapped(extractedSkill);
    }
}
