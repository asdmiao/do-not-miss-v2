package com.donotmiss.backend.aiapplication.career;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant; import java.util.List;
public final class ResumeDtos { private ResumeDtos(){}
 public record CreateResumeVersionRequest(@NotBlank @Size(max=50000) String content,@Size(max=1000) String fileReference,String resumeId){}
 public record ResumeVersionResponse(Long id,String resumeId,int version,String contentHash,ResumeParseStatus parseStatus,Instant createdAt,Instant updatedAt){}
 public record CandidateProfileResponse(Long id,Long resumeVersionId,int profileVersion,String schemaVersion,StructuredResume profile,Instant generatedAt){}
 public record CandidateEvidenceResponse(Long id,Long resumeVersionId,Long profileSnapshotId,String sourceType,String sourceId,String sourceReference,String skillCode,String claim,String evidenceText,BigDecimal confidence,Instant createdAt){}
}
