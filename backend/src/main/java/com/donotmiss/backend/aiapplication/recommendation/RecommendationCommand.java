package com.donotmiss.backend.aiapplication.recommendation;

import java.util.Map;

/** V2-neutral input; an adapter will map current AiDtos at the controller edge. */
public record RecommendationCommand(
        String userId,
        String need,
        Map<String, Object> constraints
) {
}
