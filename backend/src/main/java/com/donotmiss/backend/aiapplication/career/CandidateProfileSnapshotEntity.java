package com.donotmiss.backend.aiapplication.career;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="candidate_profile_snapshots",uniqueConstraints=@UniqueConstraint(name="uk_candidate_profile_resume",columnNames="resume_version_id"))
public class CandidateProfileSnapshotEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="user_id",nullable=false,length=80,updatable=false) private String userId;
 @Column(name="resume_version_id",nullable=false,updatable=false) private Long resumeVersionId;
 @Column(name="profile_version",nullable=false,updatable=false) private int profileVersion;
 @Column(name="schema_version",nullable=false,length=40,updatable=false) private String schemaVersion;
 @Column(name="profile_json",nullable=false,columnDefinition="json",updatable=false) private String profileJson;
 @Column(name="generated_at",nullable=false,updatable=false) private Instant generatedAt;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @PrePersist void create(){Instant now=Instant.now();if(generatedAt==null)generatedAt=now;if(createdAt==null)createdAt=now;}
 public Long getId(){return id;} public String getUserId(){return userId;} public void setUserId(String v){userId=v;} public Long getResumeVersionId(){return resumeVersionId;} public void setResumeVersionId(Long v){resumeVersionId=v;} public int getProfileVersion(){return profileVersion;} public void setProfileVersion(int v){profileVersion=v;} public String getSchemaVersion(){return schemaVersion;} public void setSchemaVersion(String v){schemaVersion=v;} public String getProfileJson(){return profileJson;} public void setProfileJson(String v){profileJson=v;} public Instant getGeneratedAt(){return generatedAt;} public void setGeneratedAt(Instant v){generatedAt=v;} public Instant getCreatedAt(){return createdAt;}
}
