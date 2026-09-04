package com.donotmiss.backend.aiapplication.agent;

import com.donotmiss.backend.aiapplication.trace.TraceService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes the explicit sequence Agent -> Action -> Tool -> Observation.
 * It has a deliberately small loop cap to prevent an accidental agent loop.
 */
@Service
public class SimpleAgentRuntime implements AgentRuntime {
    private static final int MAX_ACTIONS = 8;

    private final ToolRegistry toolRegistry;
    private final TraceService traceService;

    public SimpleAgentRuntime(ToolRegistry toolRegistry, TraceService traceService) {
        this.toolRegistry = toolRegistry;
        this.traceService = traceService;
    }

    @Override
    public AgentResult run(Agent agent, AgentContext initialContext) {
        AgentContext context = initialContext;
        List<AgentObservation> allObservations = new ArrayList<>(safeObservations(initialContext.observations()));
        AgentResult lastDecision = null;

        for (int count = 0; count < MAX_ACTIONS; count++) {
            lastDecision = agent.decide(context);
            if (lastDecision == null || lastDecision.action() == null) {
                return failed(context.state(), allObservations, "Agent returned no action.");
            }
            AgentAction action = lastDecision.action();
            if (action.type() == AgentActionType.COMPLETE || lastDecision.nextState() == AgentState.COMPLETED) {
                return new AgentResult(
                        context.state(),
                        AgentState.COMPLETED,
                        action,
                        List.copyOf(allObservations),
                        safeOutput(lastDecision.output()),
                        lastDecision.completionReason()
                );
            }
            if (action.type() != AgentActionType.CALL_TOOL) {
                return failed(context.state(), allObservations, "Unsupported runtime action: " + action.type());
            }

            AgentObservation observation = executeTool(context, action);
            allObservations.add(observation);
            context = new AgentContext(
                    context.runId(),
                    context.stepId(),
                    context.userId(),
                    context.agentName(),
                    context.goal(),
                    AgentState.OBSERVING,
                    context.inputs(),
                    List.copyOf(allObservations),
                    Instant.now()
            );
        }
        return failed(context.state(), allObservations, "Agent action limit exceeded.");
    }

    private AgentObservation executeTool(AgentContext context, AgentAction action) {
        Instant startedAt = Instant.now();
        Map<String, Object> input = safeOutput(action.arguments());
        String error = null;
        AgentToolInvocationStatus status = AgentToolInvocationStatus.SUCCEEDED;
        String summary = "";
        Map<String, Object> output = Map.of();
        try {
            AgentTool tool = toolRegistry.find(action.name())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown agent tool: " + action.name()));
            AgentToolExecutionResult result = tool.execute(new AgentToolExecutionRequest(
                    context.runId(), context.stepId(), context.userId(), input));
            output = result == null ? Map.of() : safeOutput(result.output());
            summary = result == null ? "Tool returned no result." : nullToEmpty(result.outputSummary());
        } catch (RuntimeException ex) {
            status = AgentToolInvocationStatus.FAILED;
            error = nullToEmpty(ex.getMessage());
            summary = "Tool execution failed.";
        }
        long latencyMillis = Math.max(0, java.time.Duration.between(startedAt, Instant.now()).toMillis());
        AgentToolInvocation invocation = new AgentToolInvocation(
                context.runId(), context.stepId(), action.name(), hash(String.valueOf(input)),
                "argumentKeys=" + input.keySet(), summary, status, latencyMillis,
                error == null ? "" : error, startedAt
        );
        traceService.recordToolInvocation(invocation);
        return new AgentObservation(
                action.actionId(), action.name(), summary,
                status == AgentToolInvocationStatus.SUCCEEDED ? output : Map.of("error", error == null ? "" : error), Instant.now());
    }

    private AgentResult failed(AgentState state, List<AgentObservation> observations, String reason) {
        return new AgentResult(state, AgentState.FAILED, null, List.copyOf(observations), Map.of(), reason);
    }

    private List<AgentObservation> safeObservations(List<AgentObservation> observations) {
        return observations == null ? List.of() : observations;
    }

    private Map<String, Object> safeOutput(Map<String, Object> value) {
        return value == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(value));
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
