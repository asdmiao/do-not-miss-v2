package com.donotmiss.backend.aiapplication.career.gap;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.assertThat;

class GapTraceabilityContractTest {
 @Test void persistsRequirementEvidenceAndSourceChunkReferences(){assertThat(column("requirementId").name()).isEqualTo("requirement_id");assertThat(column("candidateEvidenceIdsJson").name()).isEqualTo("candidate_evidence_ids_json");assertThat(column("sourceChunkId").name()).isEqualTo("source_chunk_id");}
 private Column column(String name){try{Field field=GapAnalysisItemEntity.class.getDeclaredField(name);return field.getAnnotation(Column.class);}catch(NoSuchFieldException ex){throw new AssertionError(ex);}}
}
