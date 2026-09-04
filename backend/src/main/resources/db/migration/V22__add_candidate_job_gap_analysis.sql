CREATE TABLE gap_analyses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(80) NOT NULL,
    resume_version_id BIGINT NOT NULL,
    candidate_profile_snapshot_id BIGINT NOT NULL,
    job_requirement_version_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    summary_json JSON NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_gap_analyses_resume FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_gap_analyses_profile FOREIGN KEY (candidate_profile_snapshot_id) REFERENCES candidate_profile_snapshots(id) ON DELETE RESTRICT,
    CONSTRAINT fk_gap_analyses_job_version FOREIGN KEY (job_requirement_version_id) REFERENCES job_requirement_versions(id) ON DELETE RESTRICT,
    INDEX idx_gap_analyses_user_job_created (user_id, job_requirement_version_id, created_at),
    INDEX idx_gap_analyses_resume_job (resume_version_id, job_requirement_version_id)
);

CREATE TABLE gap_analysis_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gap_analysis_id BIGINT NOT NULL,
    requirement_id BIGINT NOT NULL,
    skill_code VARCHAR(100) NOT NULL,
    requirement_text TEXT NOT NULL,
    importance INT NOT NULL,
    required_level VARCHAR(80) NOT NULL,
    status VARCHAR(16) NOT NULL,
    candidate_evidence_ids_json JSON NOT NULL,
    evidence_strength VARCHAR(16) NOT NULL,
    confidence DECIMAL(5,4) NOT NULL,
    reason VARCHAR(1200) NOT NULL,
    source_chunk_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_gap_analysis_items_analysis FOREIGN KEY (gap_analysis_id) REFERENCES gap_analyses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_gap_analysis_items_requirement FOREIGN KEY (requirement_id) REFERENCES structured_job_requirements(id) ON DELETE RESTRICT,
    CONSTRAINT fk_gap_analysis_items_skill FOREIGN KEY (skill_code) REFERENCES skill_dictionary(skill_code) ON DELETE RESTRICT,
    CONSTRAINT fk_gap_analysis_items_chunk FOREIGN KEY (source_chunk_id) REFERENCES job_requirement_chunks(id) ON DELETE RESTRICT,
    INDEX idx_gap_analysis_items_analysis (gap_analysis_id),
    INDEX idx_gap_analysis_items_requirement (requirement_id),
    CONSTRAINT chk_gap_analysis_items_importance CHECK (importance BETWEEN 1 AND 5)
);
