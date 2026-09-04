package com.donotmiss.backend.aiapplication.career.gap;
import com.donotmiss.backend.aiapplication.agent.*; import java.util.*;
public class GapSemanticAssessmentAgent implements Agent {
 public String name(){return "candidate-job-gap-semantic-agent";}
 public AgentResult decide(AgentContext context){if(context.state()==AgentState.PREPARING)return new AgentResult(context.state(),AgentState.TOOL_CALLING,new AgentAction("gap-semantic-assess",AgentActionType.CALL_TOOL,GapSemanticAssessmentTool.NAME,"Assess whether supplied evidence supports the supplied requirement.",context.inputs()),List.of(),Map.of(),null);if(context.state()==AgentState.OBSERVING){AgentObservation observation=context.observations().getLast();return new AgentResult(context.state(),AgentState.COMPLETED,new AgentAction("gap-semantic-complete",AgentActionType.COMPLETE,"complete","Semantic assessment observation received.",Map.of()),context.observations(),observation.data(),"Semantic assessment complete.");}return new AgentResult(context.state(),AgentState.FAILED,null,List.of(),Map.of(),"Unexpected semantic assessment state.");}
}
