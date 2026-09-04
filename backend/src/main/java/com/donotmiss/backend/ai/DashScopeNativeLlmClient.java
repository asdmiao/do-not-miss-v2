package com.donotmiss.backend.ai;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal DashScope Native text generation client.
 * It is selected only for qwen with an /api/v1 base URL.
 */
@Component
public class DashScopeNativeLlmClient {
    private static final Logger log = LoggerFactory.getLogger(DashScopeNativeLlmClient.class);
    private static final String GENERATION_PATH = "/services/aigc/multimodal-generation/generation";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String baseUrl;
    private final String provider;
    private final String model;
    private final String apiKey;

    public DashScopeNativeLlmClient(ObjectMapper objectMapper,
                                    @Value("${app.ai.provider:mock}") String provider,
                                    @Value("${app.ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                                    @Value("${app.ai.api-key:}") String apiKey,
                                    @Value("${app.ai.model:qwen-plus}") String model,
                                    @Value("${app.ai.timeout-seconds:60}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.provider = provider == null ? "mock" : provider.trim();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "qwen-plus" : model.trim();

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
        return "qwen".equalsIgnoreCase(provider)
                && baseUrl.toLowerCase().endsWith("/api/v1")
                && !apiKey.isBlank();
    }

    public String modeLabel() {
        return isEnabled() ? provider + ":" + model : "mock";
    }

    public Optional<OpenAiCompatibleLlmClient.ChatCompletion<String>> complete(
            String systemPrompt,
            String userPrompt,
            OpenAiCompatibleLlmClient.ChatRequestOptions options) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        OpenAiCompatibleLlmClient.ChatRequestOptions effective =
                options == null ? OpenAiCompatibleLlmClient.ChatRequestOptions.defaults(model) : options;
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", effective.model() == null || effective.model().isBlank()
                ? model
                : effective.model().trim());
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("messages", List.of(
                nativeMessage("system", systemPrompt),
                nativeMessage("user", userPrompt)
        ));
        requestBody.put("input", input);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("result_format", "message");
        parameters.put("enable_thinking", false);
        if (effective.temperature() != null) {
            parameters.put("temperature", effective.temperature());
        }
        if (effective.maxTokens() != null && effective.maxTokens() > 0) {
            parameters.put("max_tokens", effective.maxTokens());
        }
        requestBody.put("parameters", parameters);

        try {
            String body = restClient.post()
                    .uri(GENERATION_PATH)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                log.warn("DashScope Native response body is empty.");
                return Optional.empty();
            }

            JsonNode response = objectMapper.readTree(body);
            String content = response.path("output").path("choices").path(0)
                    .path("message").path("content").path(0).path("text").asText("");
            if (content.isBlank()) {
                log.warn("DashScope Native response has no assistant text.");
                return Optional.empty();
            }
            return Optional.of(new OpenAiCompatibleLlmClient.ChatCompletion<>(
                    content.trim(), usageOf(response)));
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            log.warn("DashScope Native request failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Object> nativeMessage(String role, String text) {
        return Map.of("role", role, "content", List.of(Map.of("text", text == null ? "" : text)));
    }

    private OpenAiCompatibleLlmClient.TokenUsage usageOf(JsonNode response) {
        JsonNode usage = response.path("usage");
        return new OpenAiCompatibleLlmClient.TokenUsage(
                nullableInt(usage, "input_tokens"),
                nullableInt(usage, "output_tokens"),
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
}
