CREATE TABLE skill_dictionary (
    skill_code VARCHAR(100) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    version VARCHAR(40) NOT NULL,
    aliases_json JSON NOT NULL,
    rubric_json JSON NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_skill_dictionary_version_active (version, active)
);

INSERT INTO skill_dictionary (skill_code, name, category, version, aliases_json, rubric_json, active, created_at, updated_at)
VALUES
('UNKNOWN', 'Unknown skill', 'SYSTEM', 'v1', JSON_ARRAY(), JSON_OBJECT('version', 'v1'), TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('UNMAPPED', 'Unmapped skill', 'SYSTEM', 'v1', JSON_ARRAY(), JSON_OBJECT('version', 'v1'), TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('JAVA_BACKEND', 'Java Backend Development', 'ENGINEERING', 'v1', JSON_ARRAY('java', 'spring', 'spring boot', 'java backend'), JSON_OBJECT('version', 'v1'), TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('AI_APPLICATION', 'AI Application Development', 'AI', 'v1', JSON_ARRAY('llm', 'ai application', '大模型应用', 'ai 应用'), JSON_OBJECT('version', 'v1'), TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('RAG_ENGINEERING', 'RAG Engineering', 'AI', 'v1', JSON_ARRAY('rag', 'retrieval augmented generation', '检索增强生成'), JSON_OBJECT('version', 'v1'), TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('AGENT_ENGINEERING', 'Agent Engineering', 'AI', 'v1', JSON_ARRAY('agent', 'ai agent', '智能体', 'tool calling'), JSON_OBJECT('version', 'v1'), TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

CREATE TABLE resume_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(80) NOT NULL,
    version_number INT NOT NULL,
    content_text LONGTEXT NOT NULL,
    file_reference VARCHAR(1000),
    content_hash CHAR(64) NOT NULL,
    parse_status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_resume_versions_resume_version UNIQUE (resume_id, version_number),
    INDEX idx_resume_versions_user_created (user_id, created_at),
    INDEX idx_resume_versions_user_resume (user_id, resume_id, version_number)
);

CREATE TABLE candidate_profile_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(80) NOT NULL,
    resume_version_id BIGINT NOT NULL,
    profile_version INT NOT NULL,
    schema_version VARCHAR(40) NOT NULL,
    profile_json JSON NOT NULL,
    generated_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_candidate_profile_resume UNIQUE (resume_version_id),
    CONSTRAINT fk_candidate_profile_resume FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE RESTRICT,
    INDEX idx_candidate_profile_user_generated (user_id, generated_at)
);

CREATE TABLE candidate_evidences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(80) NOT NULL,
    profile_snapshot_id BIGINT NOT NULL,
    resume_version_id BIGINT NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_id VARCHAR(120) NOT NULL,
    source_reference VARCHAR(240) NOT NULL,
    skill_code VARCHAR(100) NOT NULL,
    claim VARCHAR(600) NOT NULL,
    evidence_text TEXT NOT NULL,
    confidence DECIMAL(5,4) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_candidate_evidence_profile FOREIGN KEY (profile_snapshot_id) REFERENCES candidate_profile_snapshots(id) ON DELETE RESTRICT,
    CONSTRAINT fk_candidate_evidence_resume FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_candidate_evidence_skill FOREIGN KEY (skill_code) REFERENCES skill_dictionary(skill_code) ON DELETE RESTRICT,
    INDEX idx_candidate_evidence_user_created (user_id, created_at),
    INDEX idx_candidate_evidence_profile (profile_snapshot_id),
    INDEX idx_candidate_evidence_skill (skill_code)
);
