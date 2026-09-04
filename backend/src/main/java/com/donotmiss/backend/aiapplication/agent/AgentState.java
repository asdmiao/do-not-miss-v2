package com.donotmiss.backend.aiapplication.agent;

/**
 * Stable lifecycle states for a V2 agent run. Persistence is intentionally
 * delegated to the existing AgentRun/AgentRunStep infrastructure for now.
 */
public enum AgentState {
    CREATED,
    PREPARING,
    READY_FOR_ACTION,
    TOOL_CALLING,
    OBSERVING,
    UPDATING_STATE,
    COMPLETED,
    FAILED,
    ESCALATED
}
