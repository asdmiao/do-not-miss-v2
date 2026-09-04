package com.donotmiss.backend.aiapplication.career.job;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.assertThat;

class JobRequirementVersionImmutabilityTest {
    @Test void keepsVersionIdentityAndContentImmutableAfterCreation() throws Exception {
        assertThat(column("jobRequirementId").updatable()).isFalse();
        assertThat(column("userId").updatable()).isFalse();
        assertThat(column("versionNumber").updatable()).isFalse();
        assertThat(column("contentText").updatable()).isFalse();
        assertThat(column("contentHash").updatable()).isFalse();
    }
    private Column column(String name) throws Exception { Field field=JobRequirementVersionEntity.class.getDeclaredField(name); return field.getAnnotation(Column.class); }
}
