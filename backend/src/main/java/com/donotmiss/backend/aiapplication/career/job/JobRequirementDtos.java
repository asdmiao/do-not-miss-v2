package com.donotmiss.backend.aiapplication.career.job;

import jakarta.validation.constraints.*;
import java.time.Instant; import java.util.List;

public final class JobRequirementDtos {
    private JobRequirementDtos() { }
    public record CreateJobRequirementRequest(@NotBlank @Size(max=50000) String content, @NotBlank @Size(max=80) String source, String jobRequirementId) { }
    public record JobRequirementVersionResponse(Long id,String jobRequirementId,int version,String source,String contentHash,JobRequirementParseStatus parseStatus,Instant createdAt,Instant updatedAt) { }
    public record ParseJobRequirementResponse(JobRequirementVersionResponse version,String indexingIdempotencyKey,int chunkCount,int requirementCount) { }
    public record StructuredRequirementResponse(Long id,Long jobRequirementVersionId,String skillCode,int importance,String requiredLevel,String requirementText,Long sourceChunkId,Instant createdAt) { }
    public record JobSearchRequest(@NotBlank @Size(max=1000) String query,@Size(max=40) String section,@Size(max=100) String skillCode,@Min(1) @Max(30) Integer limit) { }
    public record JobSearchResponse(List<JobSearchResult> results,String mode) { }
    public record JobSearchResult(Long chunkId,double score,int rank,String section,String contentHash,String sourceReference) { }
}
