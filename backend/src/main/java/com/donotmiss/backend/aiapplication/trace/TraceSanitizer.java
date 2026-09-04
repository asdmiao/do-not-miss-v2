package com.donotmiss.backend.aiapplication.trace;

import java.util.Map;

/**
 * Mandatory boundary before AgentTraceArtifact persistence. Raw payloads stay
 * in their domain store; trace receives only the returned sanitized payload.
 */
public interface TraceSanitizer {
    TraceSanitizationResult sanitize(String artifactType, Map<String, Object> rawContent);

    TraceDataClassification classify(String fieldName);
}
