package com.donotmiss.backend.aiapplication.career.job;

import com.donotmiss.backend.aiapplication.agent.*;
import com.donotmiss.backend.aiapplication.llm.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.*;

/** The only JD extraction component that calls LlmGateway. No provider client leaks into the Agent layer. */
@Component
public class JobRequirementExtractionTool implements AgentTool {
    public static final String NAME = "extract_structured_job_requirements";
    private final LlmGateway gateway; private final ObjectMapper mapper;
    public JobRequirementExtractionTool(LlmGateway gateway, ObjectMapper mapper) { this.gateway=gateway; this.mapper=mapper; }
    @Override public String name(){return NAME;} @Override public String description(){return "Extract strict structured JD requirements from transient plain text.";} @Override public Map<String,Object> inputSchema(){return Map.of("type","object");}
    @Override public AgentToolExecutionResult execute(AgentToolExecutionRequest request) {
        Object value=request.input().get("jobText"); if (!(value instanceof String text) || text.isBlank()) throw new IllegalArgumentException("jobText is required");
        String prompt = """
                Extract a job description into JSON only: {"requirements":[{"skill":"existing code or named skill","importance":1-5,"requiredLevel":"junior|mid|senior|unspecified","requirementText":"short factual requirement","sourcePosition":1}]}. sourcePosition must match supplied chunks. Do not invent facts or skill codes. If no skill can be identified use UNKNOWN as skill. Use empty array when absent.
                """;
        Map<String,Object> metadata=new LinkedHashMap<>(); metadata.put("runId",request.runId()); metadata.put("agentName","job-requirement-extraction-agent"); metadata.put("jobVersionHash",request.input().get("jobVersionHash"));
        String chunkReferences=chunkReferences(request.input().get("chunks"));
        Optional<LlmResponse> response=gateway.complete(new LlmRequest(null,List.of(new LlmMessage(LlmMessage.Role.SYSTEM,prompt),new LlmMessage(LlmMessage.Role.USER,text+"\n\nChunk references:\n"+chunkReferences)),0.1,1800,30000L,UUID.randomUUID().toString(),"job-requirement-extraction-v1",LlmRequest.ResponseFormat.JSON_OBJECT,metadata));
        String json=response.map(LlmResponse::content).orElseGet(()->toJson(fallback(text, request.input().get("chunks"))));
        return new AgentToolExecutionResult(Map.of("structuredJobDescriptionJson",json),response.isPresent()?"Structured JD extracted by LLM.":"Structured JD extracted by rule fallback.");
    }
    private StructuredJobDescription fallback(String text,Object suppliedChunks) {
        List<StructuredJobDescription.Requirement> requirements=new ArrayList<>();
        List<String> skills=List.of("Java","Spring Boot","RAG","Agent","LLM","Python","Kubernetes");
        int position=1;
        if (suppliedChunks instanceof List<?> chunks && !chunks.isEmpty()) {
            for (Object item:chunks) { String chunk=String.valueOf(item); String found=skills.stream().filter(s->chunk.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))).findFirst().orElse("UNKNOWN"); requirements.add(new StructuredJobDescription.Requirement(found,3,"unspecified",compact(chunk,500),position++)); }
        } else requirements.add(new StructuredJobDescription.Requirement("UNKNOWN",3,"unspecified",compact(text,500),1));
        return new StructuredJobDescription(List.copyOf(requirements));
    }
    private String toJson(Object value){try{return mapper.writeValueAsString(value);}catch(Exception ex){throw new IllegalStateException("Cannot serialize fallback JD.",ex);}}
    private String chunkReferences(Object value){if(!(value instanceof List<?> chunks))return "";StringBuilder result=new StringBuilder();int position=1;for(Object chunk:chunks){result.append(position++).append(": ").append(String.valueOf(chunk)).append('\n');}return result.toString();}
    private String compact(String value,int max){String text=value==null?"":value.trim();return text.length()<=max?text:text.substring(0,max);}
}
