package com.donotmiss.backend.aiapplication.career.job;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface JobRequirementChunkRepository extends JpaRepository<JobRequirementChunkEntity,Long>{
    List<JobRequirementChunkEntity> findByJobRequirementVersionIdOrderByPositionAsc(Long jobRequirementVersionId);
    Optional<JobRequirementChunkEntity> findByIdAndJobRequirementVersionId(Long id,Long jobRequirementVersionId);
    void deleteByJobRequirementVersionId(Long jobRequirementVersionId);
}
