package com.donotmiss.backend.aiapplication.career;
import com.donotmiss.backend.aiapplication.agent.*; import java.util.*;
/** Agent only selects the extraction tool; JSON parsing remains in the application layer. */
public class ResumeExtractionAgent implements Agent{
 public String name(){return "resume-extraction-agent";}
 public AgentResult decide(AgentContext context){if(context.state()==AgentState.PREPARING){Map<String,Object> input=new java.util.LinkedHashMap<>();input.put("resumeText",context.inputs().get("resumeText"));input.put("sourceVersionHash",context.inputs().get("sourceVersionHash"));return new AgentResult(context.state(),AgentState.TOOL_CALLING,new AgentAction("resume-extract",AgentActionType.CALL_TOOL,ResumeExtractionTool.NAME,"Extract structured resume from this immutable version.",input),List.of(),Map.of(),null);} if(context.state()==AgentState.OBSERVING){AgentObservation o=context.observations().getLast();return new AgentResult(context.state(),AgentState.COMPLETED,new AgentAction("resume-complete",AgentActionType.COMPLETE,"complete","Extraction observation received.",Map.of()),context.observations(),o.data(),"Resume extraction complete.");}return new AgentResult(context.state(),AgentState.FAILED,null,List.of(),Map.of(),"Unexpected resume extraction state.");}
}
