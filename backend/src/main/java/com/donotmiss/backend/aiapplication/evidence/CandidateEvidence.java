package com.donotmiss.backend.aiapplication.evidence;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A normalized, skill-code-bound claim. sourceId remains a string to support
 * existing numeric records and future UUID/versioned sources alike.
 */
public record CandidateEvidence(
        String evidenceId,
        CandidateEvidenceSourceType sourceType,
        String sourceId,
        String skillCode,
        String claim,
        String evidenceText,
        BigDecimal confidence,
        Instant createdAt
) {
}
