package com.donotmiss.backend.aiapplication.llm;

import com.donotmiss.backend.ai.OpenAiCompatibleLlmClient;
import com.donotmiss.backend.ai.DashScopeNativeLlmClient;
import com.donotmiss.backend.aiapplication.trace.TraceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Minimal V2 gateway adapter. Provider usage is propagated when present;
 * absent provider usage remains explicitly unavailable rather than guessed.
 */
@Service
public class OpenAiCompatibleLlmAdapter implements LlmGateway {
    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmAdapter.class);

    private final OpenAiCompatibleLlmClient client;
    private final DashScopeNativeLlmClient nativeClient;
    private final TraceService traceService;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public OpenAiCompatibleLlmAdapter(OpenAiCompatibleLlmClient client,
                                      DashScopeNativeLlmClient nativeClient,
                                      TraceService traceService,
                                      ObjectMapper objectMapper) {
        this.client = client;
        this.nativeClient = nativeClient;
        this.traceService = traceService;
        this.objectMapper = objectMapper;
    }

    public OpenAiCompatibleLlmAdapter(OpenAiCompatibleLlmClient client,
                                      TraceService traceService,
                                      ObjectMapper objectMapper) {
        this(client, null, traceService, objectMapper);
    }

    @Override
    public Optional<LlmResponse> complete(LlmRequest request) {
        Instant startedAt = Instant.now();
        String requestId = valueOrGenerated(request == null ? null : request.requestId());
        try {
            if (request == null || request.messages() == null || request.messages().isEmpty()) {
                trace(requestId, request, 0, Optional.empty(), "REJECTED");
                return Optional.empty();
            }
            OpenAiCompatibleLlmClient.ChatRequestOptions options = new OpenAiCompatibleLlmClient.ChatRequestOptions(
                    request.model(), request.temperature(), request.maxTokens());
            Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> completion = callWithTimeout(
                    () -> useNativeClient()
                            ? nativeCall(request.messages(), options)
                            : request.responseFormat() == LlmRequest.ResponseFormat.JSON_OBJECT
                                    ? jsonCall(request.messages(), options)
                                    : client.chatPlainWithMetadata(renderMessages(request.messages()), options),
                    request.timeoutMillis()
            );
            long latency = elapsedMillis(startedAt);
            trace(requestId, request, latency, completion.map(OpenAiCompatibleLlmClient.ChatCompletion::usage),
                    completion.isPresent() ? "SUCCEEDED" : "FALLBACK_OR_UNAVAILABLE");
            return completion.map(value -> new LlmResponse(
                    value.content(),
                    effectiveModel(request),
                    toUsage(value.usage()),
                    "stop",
                    latency,
                    requestId,
                    providerMetadata(request)
            ));
        } catch (RuntimeException ex) {
            trace(requestId, request, elapsedMillis(startedAt), Optional.empty(), "FAILED");
            return Optional.empty();
        }
    }

    @Override
    public String modeLabel() {
        return useNativeClient() ? nativeClient.modeLabel() : client.modeLabel();
    }

    private boolean useNativeClient() {
        return nativeClient != null && nativeClient.isEnabled();
    }

    private Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> nativeCall(
            List<LlmMessage> messages,
            OpenAiCompatibleLlmClient.ChatRequestOptions options) {
        String system = messages.stream().filter(message -> message.role() == LlmMessage.Role.SYSTEM)
                .map(LlmMessage::content).findFirst().orElse("");
        String user = renderMessages(messages.stream().filter(message -> message.role() != LlmMessage.Role.SYSTEM).toList());
        return nativeClient.complete(system, user, options);
    }

    private Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> jsonCall(List<LlmMessage> messages,
                                                                                  OpenAiCompatibleLlmClient.ChatRequestOptions options) {
        String system = messages.stream().filter(message -> message.role() == LlmMessage.Role.SYSTEM)
                .map(LlmMessage::content).findFirst().orElse("");
        String user = renderMessages(messages.stream().filter(message -> message.role() != LlmMessage.Role.SYSTEM).toList());
        return client.chatForJsonWithMetadata(system, user, Map.class, options)
                .map(item -> new OpenAiCompatibleLlmClient.ChatCompletion<>(toJson(item.content()), item.usage()));
    }

    private String renderMessages(List<LlmMessage> messages) {
        return messages.stream()
                .map(message -> message.role().name() + ": " + nullToEmpty(message.content()))
                .reduce("", (left, right) -> left.isEmpty() ? right : left + "\n" + right);
    }

    private void trace(String requestId,
                       LlmRequest request,
                       long latencyMillis,
                       Optional<OpenAiCompatibleLlmClient.TokenUsage> usage,
                       String status) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("requestId", requestId);
        if (request != null && request.metadata() != null && request.metadata().get("agentName") != null) {
            trace.put("agentName", request.metadata().get("agentName"));
        }
        if (request != null && request.metadata() != null && request.metadata().get("sourceVersionHash") != null) {
            trace.put("sourceVersionHash", request.metadata().get("sourceVersionHash"));
        }
        if (request != null && request.metadata() != null && request.metadata().get("jobVersionHash") != null) {
            trace.put("jobVersionHash", request.metadata().get("jobVersionHash"));
        }
        if (request != null && request.metadata() != null && request.metadata().get("requirementId") != null) {
            trace.put("requirementId", request.metadata().get("requirementId"));
        }
        if (request != null && request.metadata() != null && request.metadata().get("evidenceCount") != null) {
            trace.put("evidenceCount", request.metadata().get("evidenceCount"));
        }
        trace.put("model", effectiveModel(request));
        if (request != null && request.promptVersion() != null) {
            trace.put("promptVersion", request.promptVersion());
        }
        trace.put("latencyMillis", latencyMillis);
        trace.put("usage", usage.map(this::usageTrace).orElse(Map.of("available", false)));
        trace.put("status", status);
        traceService.recordArtifact(readRunId(request), null, "LLM_CALL", trace);
    }

    private Long readRunId(LlmRequest request) {
        if (request == null || request.metadata() == null) {
            return null;
        }
        Object value = request.metadata().get("runId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private String effectiveModel(LlmRequest request) {
        return request == null || request.model() == null || request.model().isBlank() ? client.modeLabel() : request.model();
    }

    private String valueOrGenerated(String value) {
        return value == null || value.isBlank() ? java.util.UUID.randomUUID().toString() : value;
    }

    private long elapsedMillis(Instant startedAt) {
        return Math.max(0, Duration.between(startedAt, Instant.now()).toMillis());
    }

    private Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> callWithTimeout(
            Supplier<Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>>> invocation,
            Long timeoutMillis) {
        Instant invocationStartedAt = Instant.now();
        log.info("[LLM-DIAG] Adapter callWithTimeout invocation started timeoutMillis={}", timeoutMillis);
        if (timeoutMillis == null || timeoutMillis <= 0) {
            Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> result = invocation.get();
            log.info("[LLM-DIAG] Adapter callWithTimeout completed without Future timeout elapsedMillis={} present={}",
                    elapsedMillis(invocationStartedAt), result.isPresent());
            return result;
        }
        try {
            CompletableFuture<Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>>> future = CompletableFuture.supplyAsync(invocation);
            log.info("[LLM-DIAG] Adapter Future.get starting timeoutMillis={} elapsedMillis={}",
                    timeoutMillis, elapsedMillis(invocationStartedAt));
            Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            log.info("[LLM-DIAG] Adapter Future.get completed normally elapsedMillis={} present={}",
                    elapsedMillis(invocationStartedAt), result.isPresent());
            return result;
        } catch (TimeoutException ex) {
            Throwable cause = ex.getCause();
            log.warn("[LLM-DIAG] Adapter Future.get timed out exceptionType={} causeType={} elapsedMillis={}",
                    ex.getClass().getName(), cause == null ? "none" : cause.getClass().getName(),
                    elapsedMillis(invocationStartedAt), ex);
            return Optional.empty();
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            log.warn("[LLM-DIAG] Adapter Future.get failed exceptionType={} causeType={} elapsedMillis={}",
                    ex.getClass().getName(), cause == null ? "none" : cause.getClass().getName(),
                    elapsedMillis(invocationStartedAt), ex);
            return Optional.empty();
        }
    }

    private LlmUsage toUsage(OpenAiCompatibleLlmClient.TokenUsage usage) {
        if (usage == null) {
            return new LlmUsage(null, null, null);
        }
        return new LlmUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens());
    }

    private Map<String, Object> usageTrace(OpenAiCompatibleLlmClient.TokenUsage usage) {
        Map<String, Object> value = new LinkedHashMap<>();
        putIfPresent(value, "promptTokens", usage.promptTokens());
        putIfPresent(value, "completionTokens", usage.completionTokens());
        putIfPresent(value, "totalTokens", usage.totalTokens());
        value.put("available", !value.isEmpty());
        return Map.copyOf(value);
    }

    private String toJson(Map<?, ?> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private Map<String, Object> providerMetadata(LlmRequest request) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("mode", client.modeLabel());
        metadata.put("usageAvailable", false);
        putIfPresent(metadata, "timeoutRequestedMillis", request.timeoutMillis());
        putIfPresent(metadata, "maxTokensRequested", request.maxTokens());
        putIfPresent(metadata, "temperatureRequested", request.temperature());
        putIfPresent(metadata, "requestModel", request.model());
        return Map.copyOf(metadata);
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
