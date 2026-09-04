package com.donotmiss.backend.aiapplication.career;
import java.util.List;
/** Strict, versioned DTO used for extraction and snapshot persistence. */
public record StructuredResume(List<Education> education,List<Project> projects,List<WorkExperience> workExperience,List<Skill> skills,List<Achievement> achievements){
 public record Education(String school,String degree,String field,String period){} public record Project(String id,String name,String summary,List<String> skills,List<String> highlights){} public record WorkExperience(String id,String organization,String role,String summary,List<String> skills){} public record Skill(String name,String level){} public record Achievement(String id,String title,String detail,List<String> skills){}
}
