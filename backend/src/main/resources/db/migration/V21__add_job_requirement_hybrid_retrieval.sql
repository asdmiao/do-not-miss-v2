CREATE TABLE job_requirement_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_requirement_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(80) NOT NULL,
    version_number INT NOT NULL,
    source VARCHAR(80) NOT NULL,
    content_text LONGTEXT NOT NULL,
    content_hash CHAR(64) NOT NULL,
    parse_status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_job_requirement_versions_version UNIQUE (job_requirement_id, version_number),
    INDEX idx_job_requirement_versions_user_created (user_id, created_at),
    INDEX idx_job_requirement_versions_user_job (user_id, job_requirement_id, version_number)
);

CREATE TABLE job_requirement_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_requirement_version_id BIGINT NOT NULL,
    section VARCHAR(40) NOT NULL,
    content_text TEXT NOT NULL,
    position_number INT NOT NULL,
    content_hash CHAR(64) NOT NULL,
    index_status VARCHAR(32) NOT NULL,
    embedding_model VARCHAR(160),
    embedding_dimensions INT,
    indexed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_job_requirement_chunks_position UNIQUE (job_requirement_version_id, position_number),
    CONSTRAINT fk_job_requirement_chunks_version FOREIGN KEY (job_requirement_version_id) REFERENCES job_requirement_versions(id) ON DELETE RESTRICT,
    INDEX idx_job_requirement_chunks_version_section (job_requirement_version_id, section),
    INDEX idx_job_requirement_chunks_hash (content_hash)
);

CREATE TABLE structured_job_requirements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_requirement_version_id BIGINT NOT NULL,
    skill_code VARCHAR(100) NOT NULL,
    importance INT NOT NULL,
    required_level VARCHAR(80) NOT NULL,
    requirement_text TEXT NOT NULL,
    source_chunk_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT chk_structured_job_requirements_importance CHECK (importance BETWEEN 1 AND 5),
    CONSTRAINT fk_structured_job_requirements_version FOREIGN KEY (job_requirement_version_id) REFERENCES job_requirement_versions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_structured_job_requirements_skill FOREIGN KEY (skill_code) REFERENCES skill_dictionary(skill_code) ON DELETE RESTRICT,
    CONSTRAINT fk_structured_job_requirements_chunk FOREIGN KEY (source_chunk_id) REFERENCES job_requirement_chunks(id) ON DELETE RESTRICT,
    INDEX idx_structured_job_requirements_version (job_requirement_version_id),
    INDEX idx_structured_job_requirements_skill (skill_code)
);
