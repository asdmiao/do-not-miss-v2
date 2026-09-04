package com.donotmiss.backend.aiapplication.career.job;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="structured_job_requirements")
public class StructuredJobRequirementEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="job_requirement_version_id",nullable=false,updatable=false) private Long jobRequirementVersionId;
    @Column(name="skill_code",nullable=false,length=100,updatable=false) private String skillCode;
    @Column(nullable=false,updatable=false) private int importance;
    @Column(name="required_level",nullable=false,length=80,updatable=false) private String requiredLevel;
    @Column(name="requirement_text",nullable=false,columnDefinition="text",updatable=false) private String requirementText;
    @Column(name="source_chunk_id",nullable=false,updatable=false) private Long sourceChunkId;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @PrePersist void create(){createdAt=Instant.now();}
    public Long getId(){return id;} public Long getJobRequirementVersionId(){return jobRequirementVersionId;} public void setJobRequirementVersionId(Long v){jobRequirementVersionId=v;}
    public String getSkillCode(){return skillCode;} public void setSkillCode(String v){skillCode=v;} public int getImportance(){return importance;} public void setImportance(int v){importance=v;}
    public String getRequiredLevel(){return requiredLevel;} public void setRequiredLevel(String v){requiredLevel=v;} public String getRequirementText(){return requirementText;} public void setRequirementText(String v){requirementText=v;}
    public Long getSourceChunkId(){return sourceChunkId;} public void setSourceChunkId(Long v){sourceChunkId=v;} public Instant getCreatedAt(){return createdAt;}
}
