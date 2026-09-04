package com.donotmiss.backend.aiapplication.career.job;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_requirement_versions", uniqueConstraints = @UniqueConstraint(name = "uk_job_requirement_versions_version", columnNames = {"job_requirement_id", "version_number"}))
public class JobRequirementVersionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "job_requirement_id", nullable = false, length = 36, updatable = false) private String jobRequirementId;
    @Column(name = "user_id", nullable = false, length = 80, updatable = false) private String userId;
    @Column(name = "version_number", nullable = false, updatable = false) private int versionNumber;
    @Column(nullable = false, length = 80, updatable = false) private String source;
    @Column(name = "content_text", nullable = false, columnDefinition = "longtext", updatable = false) private String contentText;
    @Column(name = "content_hash", nullable = false, length = 64, columnDefinition = "char(64)", updatable = false) private String contentHash;
    @Enumerated(EnumType.STRING) @Column(name = "parse_status", nullable = false, length = 32) private JobRequirementParseStatus parseStatus;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void create() { Instant now = Instant.now(); createdAt = now; updatedAt = now; if (parseStatus == null) parseStatus = JobRequirementParseStatus.CREATED; }
    @PreUpdate void update() { updatedAt = Instant.now(); }
    public Long getId(){ return id; } public String getJobRequirementId(){ return jobRequirementId; } public void setJobRequirementId(String v){ jobRequirementId=v; }
    public String getUserId(){ return userId; } public void setUserId(String v){ userId=v; } public int getVersionNumber(){ return versionNumber; } public void setVersionNumber(int v){ versionNumber=v; }
    public String getSource(){ return source; } public void setSource(String v){ source=v; } public String getContentText(){ return contentText; } public void setContentText(String v){ contentText=v; }
    public String getContentHash(){ return contentHash; } public void setContentHash(String v){ contentHash=v; } public JobRequirementParseStatus getParseStatus(){ return parseStatus; } public void setParseStatus(JobRequirementParseStatus v){ parseStatus=v; }
    public Instant getCreatedAt(){ return createdAt; } public Instant getUpdatedAt(){ return updatedAt; }
}
