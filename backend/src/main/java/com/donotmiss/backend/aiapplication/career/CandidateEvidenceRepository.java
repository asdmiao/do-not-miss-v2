package com.donotmiss.backend.aiapplication.career; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CandidateEvidenceRepository extends JpaRepository<CandidateEvidenceEntity,Long>{List<CandidateEvidenceEntity> findByUserIdAndProfileSnapshotIdOrderByCreatedAtAsc(String userId,Long profileSnapshotId);}
