package com.donotmiss.backend.aiapplication.career;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="skill_dictionary") public class SkillDictionaryEntity{
 @Id @Column(name="skill_code",length=100) private String skillCode; @Column(nullable=false,length=160) private String name; @Column(nullable=false,length=80) private String category; @Column(nullable=false,length=40) private String version; @Column(name="aliases_json",nullable=false,columnDefinition="json") private String aliasesJson; @Column(name="rubric_json",nullable=false,columnDefinition="json") private String rubricJson; @Column(nullable=false) private boolean active; @Column(name="created_at",nullable=false) private Instant createdAt; @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @PrePersist void create(){Instant n=Instant.now();createdAt=n;updatedAt=n;} @PreUpdate void update(){updatedAt=Instant.now();}
 public String getSkillCode(){return skillCode;} public String getName(){return name;} public String getCategory(){return category;} public String getVersion(){return version;} public String getAliasesJson(){return aliasesJson;} public String getRubricJson(){return rubricJson;} public boolean isActive(){return active;}
}
