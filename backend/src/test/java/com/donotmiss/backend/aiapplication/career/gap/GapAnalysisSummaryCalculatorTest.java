package com.donotmiss.backend.aiapplication.career.gap;
import org.junit.jupiter.api.Test; import java.util.List; import static org.assertj.core.api.Assertions.assertThat;
class GapAnalysisSummaryCalculatorTest {
 @Test void weightsMatchesByRequirementImportanceAndCountsCriticalGaps(){var summary=GapAnalysisSummaryCalculator.calculate(List.of(new GapAnalysisSummaryCalculator.Input(5,GapStatus.MATCH),new GapAnalysisSummaryCalculator.Input(3,GapStatus.UNKNOWN),new GapAnalysisSummaryCalculator.Input(4,GapStatus.GAP)));assertThat(summary.weightedMatchRate()).isEqualByComparingTo("0.4167");assertThat(summary.criticalGapCount()).isEqualTo(1);assertThat(summary.matchedRequirements()).isEqualTo(1);}
}
