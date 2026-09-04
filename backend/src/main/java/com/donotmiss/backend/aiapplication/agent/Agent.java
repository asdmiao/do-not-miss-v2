package com.donotmiss.backend.aiapplication.agent;

/** Domain-facing contract; implementations own reasoning, not HTTP concerns. */
public interface Agent {
    String name();

    AgentResult decide(AgentContext context);
}
