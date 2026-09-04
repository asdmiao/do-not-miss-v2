package com.donotmiss.backend.aiapplication.agent;

import com.donotmiss.backend.aiapplication.demo.EchoAgent;
import com.donotmiss.backend.aiapplication.demo.GetCurrentContextTool;
import com.donotmiss.backend.aiapplication.trace.TraceService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimpleAgentRuntimeTest {
    @Test
    void completesEchoAgentAfterToolObservation() {
        ToolRegistry registry = mock(ToolRegistry.class);
        TraceService traceService = mock(TraceService.class);
        when(registry.find(GetCurrentContextTool.NAME)).thenReturn(java.util.Optional.of(new GetCurrentContextTool()));
        SimpleAgentRuntime runtime = new SimpleAgentRuntime(registry, traceService);

        AgentResult result = runtime.run(new EchoAgent(), new AgentContext(
                41L, 7L, "student-1", "echo-agent", "smoke test", AgentState.PREPARING,
                Map.of(), List.of(), Instant.now()
        ));

        assertThat(result.nextState()).isEqualTo(AgentState.COMPLETED);
        assertThat(result.observations()).hasSize(1);
        assertThat(result.observations().getFirst().source()).isEqualTo(GetCurrentContextTool.NAME);
        assertThat(result.output()).containsEntry("message", "Echo agent completed after observing tool context.");
        verify(traceService).recordToolInvocation(any(AgentToolInvocation.class));
    }
}
