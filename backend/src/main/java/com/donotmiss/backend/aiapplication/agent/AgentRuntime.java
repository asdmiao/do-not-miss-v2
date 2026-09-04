package com.donotmiss.backend.aiapplication.agent;

/** Minimal runtime: it executes one or more explicit Agent actions, not a graph framework. */
public interface AgentRuntime {
    AgentResult run(Agent agent, AgentContext initialContext);
}
