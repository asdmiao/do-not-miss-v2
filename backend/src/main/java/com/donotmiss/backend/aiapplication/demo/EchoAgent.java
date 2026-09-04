package com.donotmiss.backend.aiapplication.demo;

import com.donotmiss.backend.aiapplication.agent.Agent;
import com.donotmiss.backend.aiapplication.agent.AgentAction;
import com.donotmiss.backend.aiapplication.agent.AgentActionType;
import com.donotmiss.backend.aiapplication.agent.AgentContext;
import com.donotmiss.backend.aiapplication.agent.AgentResult;
import com.donotmiss.backend.aiapplication.agent.AgentState;

import java.util.List;
import java.util.Map;

/** Demonstrates PREPARING -> CALL_TOOL -> OBSERVING -> COMPLETE. */
public class EchoAgent implements Agent {
    @Override
    public String name() {
        return "echo-agent";
    }

    @Override
    public AgentResult decide(AgentContext context) {
        if (context.state() == AgentState.PREPARING || context.state() == AgentState.READY_FOR_ACTION) {
            return new AgentResult(
                    context.state(), AgentState.TOOL_CALLING,
                    new AgentAction("echo-context", AgentActionType.CALL_TOOL, GetCurrentContextTool.NAME,
                            "Load minimal context before responding.", Map.of()),
                    List.of(), Map.of(), null
            );
        }
        if (context.state() == AgentState.OBSERVING) {
            return new AgentResult(
                    context.state(), AgentState.COMPLETED,
                    new AgentAction("echo-complete", AgentActionType.COMPLETE, "complete",
                            "Context observation is sufficient.", Map.of()),
                    context.observations(),
                    Map.of("message", "Echo agent completed after observing tool context."),
                    "Demo completed."
            );
        }
        return new AgentResult(context.state(), AgentState.FAILED, null, List.of(), Map.of(), "Unexpected demo state.");
    }
}
