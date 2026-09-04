package com.donotmiss.backend.aiapplication.recommendation;

import java.util.List;
import java.util.Map;

public record RecommendationOutcome(
        String message,
        List<Map<String, Object>> recommendations,
        String runReference
) {
}
