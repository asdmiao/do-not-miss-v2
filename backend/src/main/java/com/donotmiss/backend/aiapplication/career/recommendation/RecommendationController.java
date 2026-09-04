package com.donotmiss.backend.aiapplication.career.recommendation;
import com.donotmiss.backend.common.CurrentUser; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/career/recommendations") public class RecommendationController {
 private final RecommendationService service; private final CurrentUser currentUser; public RecommendationController(RecommendationService service,CurrentUser currentUser){this.service=service;this.currentUser=currentUser;}
 @PostMapping public RecommendationDtos.Response recommend(@Valid @RequestBody RecommendationDtos.Request body,HttpServletRequest request){return service.recommend(currentUser.id(request),body);}
}
