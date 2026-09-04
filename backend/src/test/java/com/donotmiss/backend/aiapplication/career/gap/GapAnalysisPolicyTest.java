package com.donotmiss.backend.aiapplication.career.gap;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import static org.assertj.core.api.Assertions.assertThat;
class GapAnalysisPolicyTest {
 private final GapAnalysisPolicy policy=new GapAnalysisPolicy();
 @Test void explicitSufficientLevelMatches(){assertThat(policy.decide(policy.deterministic(true,"senior","mid"),null).status()).isEqualTo(GapStatus.MATCH);}
 @Test void explicitInsufficientLevelGaps(){assertThat(policy.decide(policy.deterministic(true,"junior","mid"),null).status()).isEqualTo(GapStatus.GAP);}
 @Test void missingEvidenceIsUnknown(){assertThat(policy.decide(policy.deterministic(false,"","mid"),null).status()).isEqualTo(GapStatus.UNKNOWN);}
 @Test void strongSemanticSupportMatches(){assertThat(policy.decide(GapDeterministicMatch.SEMANTIC_REVIEW,new GapSemanticAssessment(true,EvidenceStrength.STRONG,new BigDecimal(".91"),"direct project evidence")).status()).isEqualTo(GapStatus.MATCH);}
 @Test void strongSemanticNonSupportGaps(){assertThat(policy.decide(GapDeterministicMatch.SEMANTIC_REVIEW,new GapSemanticAssessment(false,EvidenceStrength.WEAK,new BigDecimal(".85"),"unrelated evidence")).status()).isEqualTo(GapStatus.GAP);}
 @Test void weakOrLowConfidenceSemanticSignalStaysUnknown(){assertThat(policy.decide(GapDeterministicMatch.SEMANTIC_REVIEW,new GapSemanticAssessment(true,EvidenceStrength.MODERATE,new BigDecimal(".99"),"partial")).status()).isEqualTo(GapStatus.UNKNOWN);assertThat(policy.decide(GapDeterministicMatch.SEMANTIC_REVIEW,new GapSemanticAssessment(true,EvidenceStrength.STRONG,new BigDecimal(".50"),"low confidence")).status()).isEqualTo(GapStatus.UNKNOWN);}
}
