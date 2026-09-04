package com.donotmiss.backend.aiapplication.agent;

import java.util.Optional;

/** Allow-list lookup for tools exposed to V2 agents. */
public interface ToolRegistry {
    Optional<AgentTool> find(String toolName);
}
