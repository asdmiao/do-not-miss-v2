package com.donotmiss.backend.aiapplication.career.job;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface StructuredJobRequirementRepository extends JpaRepository<StructuredJobRequirementEntity,Long>{
    List<StructuredJobRequirementEntity> findByJobRequirementVersionIdOrderByIdAsc(Long jobRequirementVersionId);
    List<StructuredJobRequirementEntity> findBySourceChunkIdIn(Collection<Long> sourceChunkIds);
    void deleteByJobRequirementVersionId(Long jobRequirementVersionId);
}
