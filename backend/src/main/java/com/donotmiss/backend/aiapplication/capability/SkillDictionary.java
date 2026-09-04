package com.donotmiss.backend.aiapplication.capability;

import java.util.Optional;

/** Future implementations may be backed by MySQL and a cached read model. */
public interface SkillDictionary {
    Optional<SkillDefinition> findByCode(String skillCode, String version);

    Optional<SkillDefinition> resolveAlias(String nameOrAlias, String version);
}
