package com.donotmiss.backend.aiapplication.demo;

import com.donotmiss.backend.aiapplication.agent.AgentTool;
import com.donotmiss.backend.aiapplication.agent.AgentToolExecutionRequest;
import com.donotmiss.backend.aiapplication.agent.AgentToolExecutionResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/** Harmless smoke-test tool; it neither reads nor writes business data. */
@Component
public class GetCurrentContextTool implements AgentTool {
    public static final String NAME = "get_current_context";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Returns minimal server context for a V2 agent runtime smoke test.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of("type", "object", "additionalProperties", false);
    }

    @Override
    public AgentToolExecutionResult execute(AgentToolExecutionRequest request) {
        return new AgentToolExecutionResult(
                Map.of("serverTime", Instant.now().toString(), "userIdPresent", request.userId() != null),
                "Server context resolved."
        );
    }
}
