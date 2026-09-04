package com.donotmiss.backend.aiapplication.career.gap;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GapAnalysisRepository extends JpaRepository<GapAnalysisEntity,Long>{Optional<GapAnalysisEntity> findTopByUserIdAndJobRequirementVersionIdAndResumeVersionIdOrderByCreatedAtDesc(String userId,Long jobRequirementVersionId,Long resumeVersionId);}
