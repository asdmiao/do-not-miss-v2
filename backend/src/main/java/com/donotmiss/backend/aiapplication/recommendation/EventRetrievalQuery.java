package com.donotmiss.backend.aiapplication.recommendation;

public record EventRetrievalQuery(
        String userId,
        String query,
        String category,
        String benefitType,
        String location,
        int limit
) {
}
