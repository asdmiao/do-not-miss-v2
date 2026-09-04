package com.donotmiss.backend.aiapplication.recommendation;

/** Future controller-facing orchestration boundary; AiService remains active. */
public interface RecommendationApplicationService {
    RecommendationOutcome recommendEvents(RecommendationCommand command);
}
