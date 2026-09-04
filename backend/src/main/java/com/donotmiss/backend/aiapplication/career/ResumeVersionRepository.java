package com.donotmiss.backend.aiapplication.career;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ResumeVersionRepository extends JpaRepository<ResumeVersionEntity,Long>{
 Optional<ResumeVersionEntity> findByIdAndUserId(Long id,String userId);
 Optional<ResumeVersionEntity> findTopByUserIdAndResumeIdOrderByVersionNumberDesc(String userId,String resumeId);
 List<ResumeVersionEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
