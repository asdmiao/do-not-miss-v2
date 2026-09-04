package com.donotmiss.backend.aiapplication.career.job;

import com.donotmiss.backend.common.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/career/jobs")
public class JobRequirementController {
    private final JobRequirementApplicationService service; private final CurrentUser currentUser;
    public JobRequirementController(JobRequirementApplicationService service,CurrentUser currentUser){this.service=service;this.currentUser=currentUser;}
    @PostMapping public JobRequirementDtos.JobRequirementVersionResponse create(@Valid @RequestBody JobRequirementDtos.CreateJobRequirementRequest body,HttpServletRequest request){return service.createVersion(currentUser.id(request),body);}
    @PostMapping("/{jobRequirementVersionId}/parse") public JobRequirementDtos.ParseJobRequirementResponse parse(@PathVariable Long jobRequirementVersionId,HttpServletRequest request){return service.parse(currentUser.id(request),jobRequirementVersionId);}
    @GetMapping public List<JobRequirementDtos.JobRequirementVersionResponse> list(HttpServletRequest request){return service.list(currentUser.id(request));}
    @GetMapping("/{jobRequirementVersionId}") public JobRequirementDtos.JobRequirementVersionResponse get(@PathVariable Long jobRequirementVersionId,HttpServletRequest request){return service.get(currentUser.id(request),jobRequirementVersionId);}
    @GetMapping("/{jobRequirementVersionId}/requirements") public List<JobRequirementDtos.StructuredRequirementResponse> requirements(@PathVariable Long jobRequirementVersionId,HttpServletRequest request){return service.requirements(currentUser.id(request),jobRequirementVersionId);}
    @PostMapping("/{jobRequirementVersionId}/search") public JobRequirementDtos.JobSearchResponse search(@PathVariable Long jobRequirementVersionId,@Valid @RequestBody JobRequirementDtos.JobSearchRequest body,HttpServletRequest request){return service.search(currentUser.id(request),jobRequirementVersionId,body);}
}
