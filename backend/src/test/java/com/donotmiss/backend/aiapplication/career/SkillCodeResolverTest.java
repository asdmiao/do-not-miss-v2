package com.donotmiss.backend.aiapplication.career;

import com.donotmiss.backend.aiapplication.capability.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SkillCodeResolverTest {
    @Test
    void returnsUnmappedInsteadOfCreatingAnLlmDefinedSkillCode() {
        SkillDictionary dictionary = mock(SkillDictionary.class);
        when(dictionary.resolveAlias("invented-skill", "v1")).thenReturn(Optional.empty());

        assertThat(new SkillCodeResolver(dictionary).resolveOrUnmapped("invented-skill")).isEqualTo("UNMAPPED");
        verify(dictionary).resolveAlias("invented-skill", "v1");
    }

    @Test
    void distinguishesUndeterminedRequirementFromNamedUnmappedSkill() {
        SkillDictionary dictionary = mock(SkillDictionary.class);
        assertThat(new SkillCodeResolver(dictionary).resolveRequirementSkill(" ")).isEqualTo("UNKNOWN");
        assertThat(new SkillCodeResolver(dictionary).resolveRequirementSkill("named-but-absent")).isEqualTo("UNMAPPED");
    }
}
