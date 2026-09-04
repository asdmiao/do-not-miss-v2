package com.donotmiss.backend.aiapplication.career.gap;
import com.donotmiss.backend.common.CurrentUser; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/career/jobs") public class GapAnalysisController {
 private final GapAnalysisService service; private final CurrentUser currentUser; public GapAnalysisController(GapAnalysisService service,CurrentUser currentUser){this.service=service;this.currentUser=currentUser;}
 @PostMapping("/{jobRequirementVersionId}/gap-analysis") public GapAnalysisDtos.Result analyze(@PathVariable Long jobRequirementVersionId,@Valid @RequestBody GapAnalysisDtos.RunRequest body,HttpServletRequest request){return service.analyze(currentUser.id(request),jobRequirementVersionId,body);}
 @GetMapping("/{jobRequirementVersionId}/gap-analysis") public GapAnalysisDtos.Result latest(@PathVariable Long jobRequirementVersionId,@RequestParam Long resumeVersionId,HttpServletRequest request){return service.latest(currentUser.id(request),jobRequirementVersionId,resumeVersionId);}
}
