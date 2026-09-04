package com.donotmiss.backend.aiapplication.career;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resume_versions", uniqueConstraints = @UniqueConstraint(name = "uk_resume_versions_resume_version", columnNames = {"resume_id", "version_number"}))
public class ResumeVersionEntity {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name="resume_id",nullable=false,length=36,updatable=false) private String resumeId;
 @Column(name="user_id",nullable=false,length=80,updatable=false) private String userId;
 @Column(name="version_number",nullable=false,updatable=false) private int versionNumber;
 @Column(name="content_text",nullable=false,columnDefinition="longtext",updatable=false) private String contentText;
 @Column(name="file_reference",length=1000,updatable=false) private String fileReference;
 @Column(name="content_hash",nullable=false,length=64,columnDefinition="char(64)",updatable=false) private String contentHash;
 @Enumerated(EnumType.STRING) @Column(name="parse_status",nullable=false,length=32) private ResumeParseStatus parseStatus;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @PrePersist void create(){Instant now=Instant.now();createdAt=now;updatedAt=now;if(parseStatus==null)parseStatus=ResumeParseStatus.CREATED;}
 @PreUpdate void update(){updatedAt=Instant.now();}
 public Long getId(){return id;} public String getResumeId(){return resumeId;} public void setResumeId(String v){resumeId=v;}
 public String getUserId(){return userId;} public void setUserId(String v){userId=v;} public int getVersionNumber(){return versionNumber;} public void setVersionNumber(int v){versionNumber=v;}
 public String getContentText(){return contentText;} public void setContentText(String v){contentText=v;} public String getFileReference(){return fileReference;} public void setFileReference(String v){fileReference=v;}
 public String getContentHash(){return contentHash;} public void setContentHash(String v){contentHash=v;} public ResumeParseStatus getParseStatus(){return parseStatus;} public void setParseStatus(ResumeParseStatus v){parseStatus=v;}
 public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
