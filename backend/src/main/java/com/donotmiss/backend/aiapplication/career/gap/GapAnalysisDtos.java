package com.donotmiss.backend.aiapplication.career.gap;
import jakarta.validation.constraints.NotNull; import java.math.BigDecimal; import java.time.Instant; import java.util.List;
public final class GapAnalysisDtos { private GapAnalysisDtos(){}
 public record RunRequest(@NotNull Long resumeVersionId){}
 public record Summary(int totalRequirements,int matchedRequirements,int gapRequirements,int unknownRequirements,BigDecimal weightedMatchRate,int criticalGapCount){}
 public record EvidenceReference(Long evidenceId,Long resumeVersionId,String skillCode,String claim,String sourceReference,BigDecimal confidence){}
 public record Item(Long id,Long requirementId,String skillCode,String requirementText,int importance,String requiredLevel,GapStatus status,List<EvidenceReference> candidateEvidence,EvidenceStrength evidenceStrength,BigDecimal confidence,String reason,Long sourceChunkId){}
 public record Result(Long id,Long resumeVersionId,Long candidateProfileSnapshotId,Long jobRequirementVersionId,GapStatus status,Summary summary,List<Item> items,Instant createdAt){}
}
