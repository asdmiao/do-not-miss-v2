package com.donotmiss.backend.aiapplication.retrieval.job;

import com.donotmiss.backend.aiapplication.retrieval.job.evaluation.JobRetrievalEvaluation; import org.junit.jupiter.api.Test; import java.util.*; import static org.assertj.core.api.Assertions.assertThat;
class JobRetrievalContractsTest {
 @Test void fusesRanksWithoutPretendingVectorWasPresent(){assertThat(ReciprocalRankFusion.fuse(List.of(new RankedJobChunk(1L,10)),List.of(),10)).extracting(RankedJobChunk::chunkId).containsExactly(1L);}
 @Test void calculatesRecallMrrAndNdcg(){var result=new JobRequirementRetrievalService.JobRetrievalResult(7L,1,1,"REQUIREMENTS","h","r");var metrics=JobRetrievalEvaluation.evaluate(List.of(new JobRetrievalEvaluation.EvalCase("x","java",Map.of(7L,3))),Map.of("x",List.of(result)),3);assertThat(metrics.recallAtK()).isEqualTo(1);assertThat(metrics.mrr()).isEqualTo(1);assertThat(metrics.nDCG()).isEqualTo(1);}
}
