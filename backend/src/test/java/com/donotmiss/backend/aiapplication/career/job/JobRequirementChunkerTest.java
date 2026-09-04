package com.donotmiss.backend.aiapplication.career.job;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JobRequirementChunkerTest {
    @Test void producesStableSectionAwareChunks() {
        var chunks=new JobRequirementChunker().chunk("岗位职责：\n构建 API\n任职要求：\n熟悉 Java 和 Spring Boot\n加分项：\nRAG 项目");
        assertThat(chunks).extracting(JobRequirementChunker.ChunkDraft::section).containsExactly(JobRequirementChunkSection.RESPONSIBILITIES,JobRequirementChunkSection.REQUIREMENTS,JobRequirementChunkSection.PREFERRED);
        assertThat(chunks).extracting(JobRequirementChunker.ChunkDraft::position).containsExactly(1,2,3);
        assertThat(chunks.get(1).contentHash()).hasSize(64);
    }
}
