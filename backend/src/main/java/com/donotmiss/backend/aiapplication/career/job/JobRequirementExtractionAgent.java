package com.donotmiss.backend.aiapplication.career.job;

import com.donotmiss.backend.aiapplication.agent.*;
import java.util.*;

/** Chooses one extraction tool; the runtime owns Tool -> Observation -> state transition. */
public class JobRequirementExtractionAgent implements Agent {
    @Override public String name() { return "job-requirement-extraction-agent"; }
    @Override public AgentResult decide(AgentContext context) {
        if (context.state() == AgentState.PREPARING) {
            Map<String,Object> input = new LinkedHashMap<>();
            input.put("jobText", context.inputs().get("jobText"));
            input.put("chunks", context.inputs().get("chunks"));
            input.put("jobVersionHash", context.inputs().get("jobVersionHash"));
            return new AgentResult(context.state(), AgentState.TOOL_CALLING,
                    new AgentAction("job-requirement-extract", AgentActionType.CALL_TOOL, JobRequirementExtractionTool.NAME,
                            "Extract strict requirements against supplied chunk positions.", input), List.of(), Map.of(), null);
        }
        if (context.state() == AgentState.OBSERVING) {
            AgentObservation observation = context.observations().getLast();
            return new AgentResult(context.state(), AgentState.COMPLETED,
                    new AgentAction("job-requirement-complete", AgentActionType.COMPLETE, "complete", "Extraction observation received.", Map.of()),
                    context.observations(), observation.data(), "Job requirement extraction complete.");
        }
        return new AgentResult(context.state(), AgentState.FAILED, null, List.of(), Map.of(), "Unexpected job extraction state.");
    }
}
