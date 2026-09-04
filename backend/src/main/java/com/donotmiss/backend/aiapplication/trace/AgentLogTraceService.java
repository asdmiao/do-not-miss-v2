package com.donotmiss.backend.aiapplication.trace;

import com.donotmiss.backend.agentlog.AgentTraceArtifactService;
import com.donotmiss.backend.aiapplication.agent.AgentToolInvocation;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** V2 adapter over the existing trace store; it always sanitizes before persistence. */
@Service
public class AgentLogTraceService implements TraceService {
    private final TraceSanitizer sanitizer;
    private final AgentTraceArtifactService artifactService;

    public AgentLogTraceService(TraceSanitizer sanitizer,
                                AgentTraceArtifactService artifactService) {
        this.sanitizer = sanitizer;
        this.artifactService = artifactService;
    }

    @Override
    public void recordArtifact(Long runId, Long stepId, String artifactType, Map<String, Object> rawContent) {
        if (runId == null) {
            return;
        }
        TraceSanitizationResult sanitized = sanitizer.sanitize(artifactType, rawContent);
        artifactService.record(runId, null, artifactType, sanitized.sanitizedContent(),
                "sanitized=true, droppedFields=" + sanitized.droppedFieldCount());
    }

    @Override
    public void recordToolInvocation(AgentToolInvocation invocation) {
        if (invocation == null || invocation.runId() == null) {
            return;
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("runId", invocation.runId());
        content.put("stepId", invocation.stepId());
        content.put("toolName", invocation.toolName());
        content.put("inputHash", invocation.inputHash());
        content.put("sanitizedInput", invocation.sanitizedInput());
        content.put("outputSummary", invocation.outputSummary());
        if (invocation.status() != null) {
            content.put("status", invocation.status().name());
        }
        content.put("latencyMillis", invocation.latencyMillis());
        content.put("error", invocation.error());
        recordArtifact(invocation.runId(), invocation.stepId(), "TOOL_INVOCATION", content);
    }
}
