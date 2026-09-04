package com.donotmiss.backend.aiapplication.career.gap;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GapAnalysisItemRepository extends JpaRepository<GapAnalysisItemEntity,Long>{List<GapAnalysisItemEntity> findByGapAnalysisIdOrderByRequirementIdAsc(Long gapAnalysisId);}
