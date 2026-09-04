package com.donotmiss.backend.aiapplication.trace;

import com.donotmiss.backend.aiapplication.agent.AgentToolInvocation;

import java.util.Map;

/** Application facade that will combine sanitization with existing AgentRun tracing. */
public interface TraceService {
    void recordArtifact(Long runId, Long stepId, String artifactType, Map<String, Object> rawContent);

    void recordToolInvocation(AgentToolInvocation invocation);
}
