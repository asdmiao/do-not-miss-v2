package com.donotmiss.backend.aiapplication.recommendation;

import java.util.List;
import java.util.Map;

/** A normalized retrieval result; existing RetrievedEvent is adapted at the boundary. */
public record RetrievalDocument(
        String documentId,
        double score,
        List<String> evidence,
        Map<String, Object> attributes
) {
}
