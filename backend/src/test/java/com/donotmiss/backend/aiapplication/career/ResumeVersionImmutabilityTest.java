package com.donotmiss.backend.aiapplication.career;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeVersionImmutabilityTest {
    @Test
    void makesVersionContentAndIdentityColumnsNonUpdatable() throws Exception {
        for (String fieldName : new String[]{"resumeId", "userId", "versionNumber", "contentText", "fileReference", "contentHash"}) {
            Field field = ResumeVersionEntity.class.getDeclaredField(fieldName);
            assertThat(field.getAnnotation(Column.class).updatable()).isFalse();
        }
    }
}
