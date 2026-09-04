package com.donotmiss.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenAiCompatibleLlmClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmClient.class);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String baseUrl;
    private final String provider;
    private final String model;
    private final String embeddingModel;
    private final String apiKey;

    public OpenAiCompatibleLlmClient(ObjectMapper objectMapper,
                                     @Value("${app.ai.provider:mock}") String provider,
                                     @Value("${app.ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                                     @Value("${app.ai.api-key:}") String apiKey,
                                     @Value("${app.ai.model:qwen-plus}") String model,
                                     @Value("${app.ai.embedding-model:text-embedding-v4}") String embeddingModel,
                                     @Value("${app.ai.timeout-seconds:60}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.provider = provider == null ? "mock" : provider.trim();
        this.model = model == null || model.isBlank() ? "qwen-plus" : model.trim();
        this.embeddingModel = embeddingModel == null || embeddingModel.isBlank() ? "text-embedding-v4" : embeddingModel.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = trimTrailingSlash(baseUrl);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(timeoutSeconds, 1));
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .baseUrl(this.baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isEnabled() {
        return !"mock".equalsIgnoreCase(provider) && !apiKey.isBlank();
    }

    public String modeLabel() {
        return isEnabled() ? provider + ":" + model : "mock";
    }

    public String embeddingModeLabel() {
        return isEnabled() ? provider + ":" + embeddingModel : "mock";
    }

    public Optional<String> chatPlain(String userPrompt) {
        return chatPlain(userPrompt, ChatRequestOptions.defaults(model));
    }

    /** Backward-compatible request override used by the V2 LlmGateway adapter. */
    public Optional<String> chatPlain(String userPrompt, ChatRequestOptions options) {
        return chatPlainWithMetadata(userPrompt, options).map(ChatCompletion::content);
    }

    /** Returns provider token counts when the OpenAI-compatible response supplies them. */
    public Optional<ChatCompletion<String>> chatPlainWithMetadata(String userPrompt, ChatRequestOptions options) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        Map<String, Object> requestBody = requestBody(options, List.of(
                Map.of("role", "user", "content", userPrompt)
        ));

        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            String content = response == null
                    ? ""
                    : response.path("choices").path(0).path("message").path("content").asText("");

            if (content.isBlank()) {
                log.warn("LLM plain response has no assistant content.");
                return Optional.empty();
            }

            return Optional.of(new ChatCompletion<>(content.trim(), usageOf(response)));
        } catch (RestClientException ex) {
            log.warn("LLM plain request failed, falling back to local mock rules: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public <T> Optional<T> chatForJson(String systemPrompt, String userPrompt, Class<T> responseType) {
        return chatForJson(systemPrompt, userPrompt, responseType, ChatRequestOptions.defaults(model));
    }

    /** Backward-compatible request override used by the V2 LlmGateway adapter. */
    public <T> Optional<T> chatForJson(String systemPrompt,
                                       String userPrompt,
                                       Class<T> responseType,
                                       ChatRequestOptions options) {
        return chatForJsonWithMetadata(systemPrompt, userPrompt, responseType, options)
                .map(ChatCompletion::content);
    }

    /** Returns parsed JSON plus provider token counts when present. */
    public <T> Optional<ChatCompletion<T>> chatForJsonWithMetadata(String systemPrompt,
                                                                    String userPrompt,
                                                                    Class<T> responseType,
                                                                    ChatRequestOptions options) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        Map<String, Object> requestBody = requestBody(options, List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("response_format", Map.of("type", "json_object"));

        Instant startedAt = Instant.now();
        log.info("[LLM-DIAG] Chat HTTP request started url={} model={}",
                baseUrl + "/chat/completions", effectiveModel(options));
        try {
            log.info("[LLM-DIAG] Chat HTTP body read/conversion started elapsedMillis={}",
                    elapsedMillis(startedAt));
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
            log.info("[LLM-DIAG] Chat HTTP response arrived elapsedMillis={}",
                    elapsedMillis(startedAt));
            log.info("[LLM-DIAG] Chat HTTP body read/conversion completed bodyPresent={} elapsedMillis={}",
                    response != null, elapsedMillis(startedAt));

            String content = response == null
                    ? ""
                    : response.path("choices").path(0).path("message").path("content").asText("");

            if (content.isBlank()) {
                log.warn("LLM response has no assistant content.");
                return Optional.empty();
            }

            return Optional.of(new ChatCompletion<>(
                    objectMapper.readValue(extractJsonObject(content), responseType), usageOf(response)
            ));
        } catch (RestClientException ex) {
            if (ex instanceof RestClientResponseException responseException) {
                log.warn("[LLM-DIAG] Chat HTTP request failed exceptionType={} message={} status={} contentType={} elapsedMillis={}",
                        ex.getClass().getName(), ex.getMessage(), responseException.getStatusCode().value(),
                        responseException.getResponseHeaders() == null
                                ? null
                                : responseException.getResponseHeaders().getContentType(),
                        elapsedMillis(startedAt), ex);
            } else {
                log.warn("[LLM-DIAG] Chat HTTP request failed exceptionType={} message={} status=unavailable contentType=unavailable elapsedMillis={}",
                        ex.getClass().getName(), ex.getMessage(), elapsedMillis(startedAt), ex);
            }
            log.warn("LLM request failed, falling back to local mock rules: {}", ex.getMessage());
            return Optional.empty();
        } catch (JsonProcessingException ex) {
            log.warn("[LLM-DIAG] Chat JSON parsing failed exceptionType={} message={} elapsedMillis={}",
                    ex.getClass().getName(), ex.getMessage(), elapsedMillis(startedAt), ex);
            log.warn("LLM request failed, falling back to local mock rules: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<Double>> embedding(String input) {
        if (!isEnabled() || input == null || input.isBlank()) {
            return Optional.empty();
        }

        Map<String, Object> requestBody = Map.of(
                "model", embeddingModel,
                "input", input
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/embeddings")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode embedding = response == null
                    ? null
                    : response.path("data").path(0).path("embedding");

            if (embedding == null || !embedding.isArray() || embedding.isEmpty()) {
                log.warn("Embedding response has no vector content.");
                return Optional.empty();
            }

            List<Double> vector = new ArrayList<>();
            for (JsonNode item : embedding) {
                if (!item.isNumber()) {
                    return Optional.empty();
                }
                vector.add(item.asDouble());
            }
            return Optional.of(vector);
        } catch (RestClientException ex) {
            log.warn("Embedding request failed, vector retrieval will be skipped: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String extractJsonObject(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private Map<String, Object> requestBody(ChatRequestOptions options, List<Map<String, String>> messages) {
        ChatRequestOptions effective = options == null ? ChatRequestOptions.defaults(model) : options;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", effective.model() == null || effective.model().isBlank() ? model : effective.model().trim());
        body.put("temperature", effective.temperature() == null ? 0.2 : effective.temperature());
        if (effective.maxTokens() != null && effective.maxTokens() > 0) {
            body.put("max_tokens", effective.maxTokens());
        }
        body.put("messages", messages);
        return body;
    }

    public record ChatRequestOptions(String model, Double temperature, Integer maxTokens) {
        public static ChatRequestOptions defaults(String model) {
            return new ChatRequestOptions(model, 0.2, null);
        }
    }

    public record TokenUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
    }

    public record ChatCompletion<T>(T content, TokenUsage usage) {
    }

    private TokenUsage usageOf(JsonNode response) {
        JsonNode usage = response == null ? null : response.path("usage");
        if (usage == null || usage.isMissingNode() || usage.isNull()) {
            return new TokenUsage(null, null, null);
        }
        return new TokenUsage(
                nullableInt(usage, "prompt_tokens"),
                nullableInt(usage, "completion_tokens"),
                nullableInt(usage, "total_tokens")
        );
    }

    private Integer nullableInt(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isInt() || value.isLong() ? value.asInt() : null;
    }

    private String trimTrailingSlash(String value) {
        String normalized = value == null || value.isBlank()
                ? "https://dashscope.aliyuncs.com/compatible-mode/v1"
                : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String effectiveModel(ChatRequestOptions options) {
        return options == null || options.model() == null || options.model().isBlank()
                ? model
                : options.model().trim();
    }

    private long elapsedMillis(java.time.Instant startedAt) {
        return Math.max(0, Duration.between(startedAt, java.time.Instant.now()).toMillis());
    }
}
