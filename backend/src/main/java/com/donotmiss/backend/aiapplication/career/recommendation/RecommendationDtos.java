package com.donotmiss.backend.aiapplication.career.recommendation;
import com.donotmiss.backend.aiapplication.career.gap.*; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.util.List;
public final class RecommendationDtos {private RecommendationDtos(){}
 public record Request(@NotNull Long resumeVersionId,@NotEmpty @Size(max=50) List<@NotNull Long> jobRequirementVersionIds,@Min(1) @Max(20) Integer topK){}
 public record RequirementInsight(Long requirementId,String skillCode,String requirementText,int importance,String requiredLevel,GapStatus status,EvidenceStrength evidenceStrength,BigDecimal confidence,String reason,Long sourceChunkId,List<GapAnalysisDtos.EvidenceReference> candidateEvidence){}
 public record Item(Long jobRequirementVersionId,String jobRequirementId,int jobVersion,BigDecimal recommendationScore,BigDecimal weightedMatchRate,int matchCount,int gapCount,int unknownCount,int criticalGapCount,String summary,List<RequirementInsight> topMatches,List<RequirementInsight> topGaps,List<RequirementInsight> topUnknowns){}
 public record Response(Long resumeVersionId,List<Item> recommendations){}
}
