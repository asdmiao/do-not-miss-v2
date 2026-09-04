package com.donotmiss.backend.aiapplication.trace;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultTraceSanitizerTest {
    private final DefaultTraceSanitizer sanitizer = new DefaultTraceSanitizer();

    @Test
    void dropsRawPromptAndResponseWhileRetainingOperationalMetadata() {
        TraceSanitizationResult result = sanitizer.sanitize("LLM_CALL", Map.of(
                "requestId", "request-1",
                "agentName", "echo-agent",
                "promptVersion", "v1",
                "prompt", "raw private prompt",
                "response", "raw private response",
                "userId", "student-1"
        ));

        assertThat(result.redacted()).isTrue();
        assertThat(result.sanitizedContent())
                .containsEntry("requestId", "request-1")
                .containsEntry("agentName", "echo-agent")
                .containsEntry("userId", "***")
                .doesNotContainKeys("prompt", "response");
        assertThat(result.droppedFieldCount()).isEqualTo(2);
    }

    @Test
    void permitsOnlyNumericUsageAndHashesToolOutputSummary() {
        TraceSanitizationResult result = sanitizer.sanitize("TOOL_INVOCATION", Map.of(
                "usage", Map.of("totalTokens", 18, "private", "must not persist"),
                "outputSummary", "potentially private output"
        ));

        assertThat(result.sanitizedContent().get("usage"))
                .isEqualTo(Map.of("totalTokens", 18));
        assertThat(result.sanitizedContent()).containsKey("outputSummaryHash")
                .doesNotContainKey("outputSummary");
    }

    @Test
    void permitsJobOperationalMetadataButDropsRawJobContent() {
        TraceSanitizationResult result = sanitizer.sanitize("JOB_RETRIEVAL", Map.of(
                "jobVersionHash", "version-hash", "queryHash", "query-hash", "chunkId", 8L,
                "jobContent", "private JD"));
        assertThat(result.sanitizedContent()).containsEntry("jobVersionHash", "version-hash").containsEntry("chunkId", 8L)
                .doesNotContainKey("jobContent");
    }

    @Test
    void permitsGapCountsButDropsRequirementAndEvidenceText() {
        TraceSanitizationResult result = sanitizer.sanitize("GAP_SEMANTIC", Map.of(
                "requirementId", 9L, "evidenceCount", 2,
                "requirementText", "private JD", "evidenceText", "private resume evidence"));
        assertThat(result.sanitizedContent()).containsEntry("requirementId", 9L).containsEntry("evidenceCount", 2)
                .doesNotContainKeys("requirementText", "evidenceText");
    }
}
