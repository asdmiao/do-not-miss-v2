package com.donotmiss.backend.aiapplication.career.job;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface JobRequirementVersionRepository extends JpaRepository<JobRequirementVersionEntity,Long>{
    Optional<JobRequirementVersionEntity> findByIdAndUserId(Long id,String userId);
    Optional<JobRequirementVersionEntity> findTopByUserIdAndJobRequirementIdOrderByVersionNumberDesc(String userId,String jobRequirementId);
    List<JobRequirementVersionEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
