package com.donotmiss.backend.aiapplication.agent;

import java.util.Map;

/**
 * A server-owned, allow-listed tool. Agents choose a tool name; applications
 * validate ownership, authorization and input before execution.
 */
public interface AgentTool {
    String name();

    String description();

    Map<String, Object> inputSchema();

    AgentToolExecutionResult execute(AgentToolExecutionRequest request);
}
