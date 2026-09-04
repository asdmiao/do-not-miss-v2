package com.donotmiss.backend.aiapplication.career.recommendation;
import com.donotmiss.backend.aiapplication.career.ResumeVersionRepository; import com.donotmiss.backend.aiapplication.career.job.JobRequirementVersionRepository; import org.junit.jupiter.api.Test; import java.lang.reflect.Method; import static org.assertj.core.api.Assertions.assertThat;
class RecommendationIsolationContractTest {
 @Test void repositoriesExposeUserScopedVersionLookups() throws Exception {Method resume=ResumeVersionRepository.class.getMethod("findByIdAndUserId",Long.class,String.class);Method job=JobRequirementVersionRepository.class.getMethod("findByIdAndUserId",Long.class,String.class);assertThat(resume).isNotNull();assertThat(job).isNotNull();}
}
