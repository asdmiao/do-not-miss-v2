package com.donotmiss.backend.aiapplication.career.gap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

/** 12 manually-labelled candidate-evidence/job-requirement cases; exercises the same policy used by the service. */
class GapAnalysisFixtureTest {
 @Test void validatesMatchGapAndUnknownFixtureCases() throws Exception {
  JsonNode cases=new ObjectMapper().readTree(new ClassPathResource("gap-analysis-eval-fixture.json").getInputStream()); GapAnalysisPolicy policy=new GapAnalysisPolicy(); int total=0,correct=0;
  for(JsonNode item:cases){boolean hasEvidence=!item.path("candidateEvidenceSkill").isNull();GapDeterministicMatch deterministic=policy.deterministic(hasEvidence,"",item.path("requiredLevel").asText());GapSemanticAssessment semantic=hasEvidence?new GapSemanticAssessment(item.path("semanticSupported").asBoolean(),EvidenceStrength.valueOf(item.path("strength").asText()),item.path("confidence").decimalValue(),"fixture"):null;GapStatus actual=policy.decide(deterministic,semantic).status();if(actual==GapStatus.valueOf(item.path("expected").asText()))correct++;total++;}
  assertThat(total).isBetween(10,20); assertThat(correct).isEqualTo(total);
 }
}
