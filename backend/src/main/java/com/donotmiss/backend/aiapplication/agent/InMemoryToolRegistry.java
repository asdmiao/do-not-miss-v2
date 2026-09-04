package com.donotmiss.backend.aiapplication.agent;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Minimal Spring registry; duplicate names fail fast at application startup. */
@Component
public class InMemoryToolRegistry implements ToolRegistry {
    private final Map<String, AgentTool> tools;

    public InMemoryToolRegistry(List<AgentTool> tools) {
        Map<String, AgentTool> byName = new LinkedHashMap<>();
        for (AgentTool tool : tools) {
            AgentTool existing = byName.putIfAbsent(tool.name(), tool);
            if (existing != null) {
                throw new IllegalStateException("Duplicate V2 agent tool: " + tool.name());
            }
        }
        this.tools = Map.copyOf(byName);
    }

    @Override
    public Optional<AgentTool> find(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }
}
