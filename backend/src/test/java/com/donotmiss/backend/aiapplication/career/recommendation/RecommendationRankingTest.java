package com.donotmiss.backend.aiapplication.career.recommendation;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.util.*; import static org.assertj.core.api.Assertions.assertThat;
class RecommendationRankingTest {
 @Test void ranksMultipleJobsByScoreAndLimitsTopK(){var ranked=RecommendationRanking.top(List.of(item(3L,".62"),item(2L,".75"),item(1L,".90")),3);assertThat(ranked).extracting(RecommendationDtos.Item::jobRequirementVersionId).containsExactly(1L,2L,3L);}
 private RecommendationDtos.Item item(Long id,String score){return new RecommendationDtos.Item(id,"job",1,new BigDecimal(score),new BigDecimal(score),0,0,0,0,"fixture",List.of(),List.of(),List.of());}
}
