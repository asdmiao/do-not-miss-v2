package com.donotmiss.backend.aiapplication.career;

import com.donotmiss.backend.aiapplication.agent.*;
import com.donotmiss.backend.aiapplication.llm.*;
import com.donotmiss.backend.aiapplication.trace.TraceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResumeExtractionRuntimeTest {
    @Test
    void runsResumeAgentThroughRuntimeToolAndGateway() {
        LlmGateway gateway = mock(LlmGateway.class);
        TraceService trace = mock(TraceService.class);
        when(gateway.complete(any())).thenReturn(Optional.of(new LlmResponse(
                "{\"education\":[],\"projects\":[{\"id\":\"p1\",\"name\":\"API\",\"summary\":\"Built Java API\",\"skills\":[\"Java\"],\"highlights\":[]}],\"workExperience\":[],\"skills\":[{\"name\":\"Java\",\"level\":\"basic\"}],\"achievements\":[]}",
                "mock", new LlmUsage(10, 8, 18), "stop", 1, "request", Map.of()
        )));
        ResumeExtractionTool tool = new ResumeExtractionTool(gateway, new ObjectMapper());
        ToolRegistry registry = name -> Optional.of(tool);
        AgentRuntime runtime = new SimpleAgentRuntime(registry, trace);

        AgentResult result = runtime.run(new ResumeExtractionAgent(), new AgentContext(
                1L, 2L, "student", "resume-extraction-agent", "extract", AgentState.PREPARING,
                Map.of("resumeText", "Java project", "sourceVersionHash", "hash"), List.of(), Instant.now()
        ));

        assertThat(result.nextState()).isEqualTo(AgentState.COMPLETED);
        assertThat(result.output()).containsKey("structuredResumeJson");
        verify(gateway).complete(any(LlmRequest.class));
        verify(trace).recordToolInvocation(any(AgentToolInvocation.class));
    }
}
