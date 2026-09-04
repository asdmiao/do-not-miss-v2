package com.donotmiss.backend.aiapplication.career.gap;
import org.junit.jupiter.api.Test; import java.lang.reflect.Method; import static org.assertj.core.api.Assertions.assertThat;
class GapAnalysisIsolationContractTest {
 @Test void latestAnalysisIsScopedToUserJobAndResumeVersion() throws Exception {Method method=GapAnalysisRepository.class.getMethod("findTopByUserIdAndJobRequirementVersionIdAndResumeVersionIdOrderByCreatedAtDesc",String.class,Long.class,Long.class);assertThat(method).isNotNull();}
}
