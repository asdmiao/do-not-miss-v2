package com.donotmiss.backend.aiapplication.career.job;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "job_requirement_chunks", uniqueConstraints = @UniqueConstraint(name = "uk_job_requirement_chunks_position", columnNames = {"job_requirement_version_id", "position_number"}))
public class JobRequirementChunkEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="job_requirement_version_id", nullable=false, updatable=false) private Long jobRequirementVersionId;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=40, updatable=false) private JobRequirementChunkSection section;
    @Column(name="content_text", nullable=false, columnDefinition="text", updatable=false) private String content;
    @Column(name="position_number", nullable=false, updatable=false) private int position;
    @Column(name="content_hash", nullable=false, length=64, columnDefinition="char(64)", updatable=false) private String contentHash;
    @Enumerated(EnumType.STRING) @Column(name="index_status", nullable=false, length=32) private JobRequirementChunkIndexStatus indexStatus;
    @Column(name="embedding_model", length=160) private String embeddingModel;
    @Column(name="embedding_dimensions") private Integer embeddingDimensions;
    @Column(name="indexed_at") private Instant indexedAt;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @PrePersist void create(){ Instant now=Instant.now(); createdAt=now; updatedAt=now; if(indexStatus==null) indexStatus=JobRequirementChunkIndexStatus.PENDING; } @PreUpdate void update(){updatedAt=Instant.now();}
    public Long getId(){return id;} public Long getJobRequirementVersionId(){return jobRequirementVersionId;} public void setJobRequirementVersionId(Long v){jobRequirementVersionId=v;}
    public JobRequirementChunkSection getSection(){return section;} public void setSection(JobRequirementChunkSection v){section=v;} public String getContent(){return content;} public void setContent(String v){content=v;}
    public int getPosition(){return position;} public void setPosition(int v){position=v;} public String getContentHash(){return contentHash;} public void setContentHash(String v){contentHash=v;}
    public JobRequirementChunkIndexStatus getIndexStatus(){return indexStatus;} public void setIndexStatus(JobRequirementChunkIndexStatus v){indexStatus=v;}
    public String getEmbeddingModel(){return embeddingModel;} public void setEmbeddingModel(String v){embeddingModel=v;} public Integer getEmbeddingDimensions(){return embeddingDimensions;} public void setEmbeddingDimensions(Integer v){embeddingDimensions=v;} public Instant getIndexedAt(){return indexedAt;} public void setIndexedAt(Instant v){indexedAt=v;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
