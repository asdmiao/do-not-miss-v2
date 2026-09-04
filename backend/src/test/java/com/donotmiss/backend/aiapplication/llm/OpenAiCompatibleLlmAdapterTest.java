package com.donotmiss.backend.aiapplication.llm;

import com.donotmiss.backend.ai.OpenAiCompatibleLlmClient;
import com.donotmiss.backend.ai.DashScopeNativeLlmClient;
import com.donotmiss.backend.aiapplication.trace.TraceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class OpenAiCompatibleLlmAdapterTest {
    @Test
    void delegatesPlainRequestAndWritesOnlyOperationalTraceMetadata() {
        OpenAiCompatibleLlmClient client = mock(OpenAiCompatibleLlmClient.class);
        TraceService traceService = mock(TraceService.class);
        when(client.chatPlainWithMetadata(any(), any())).thenReturn(Optional.of(
                new OpenAiCompatibleLlmClient.ChatCompletion<>(
                        "adapter response", new OpenAiCompatibleLlmClient.TokenUsage(11, 7, 18)
                )
        ));
        when(client.modeLabel()).thenReturn("mock");
        OpenAiCompatibleLlmAdapter adapter = new OpenAiCompatibleLlmAdapter(client, traceService, new ObjectMapper());

        Optional<LlmResponse> response = adapter.complete(new LlmRequest(
                "qwen-plus", List.of(new LlmMessage(LlmMessage.Role.USER, "private input")), 0.2,
                400, 2_000L, "request-1", "prompt-v1", LlmRequest.ResponseFormat.TEXT,
                Map.of("runId", 9L, "agentName", "echo-agent")
        ));

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().content()).isEqualTo("adapter response");
        assertThat(response.orElseThrow().usage().totalTokens()).isEqualTo(18);
        assertThat(response.orElseThrow().requestId()).isEqualTo("request-1");
        verify(client).chatPlainWithMetadata(any(), any());
        verify(traceService).recordArtifact(eq(9L), eq(null), eq("LLM_CALL"), any());
    }

    @Test
    void routesEnabledQwenApiV1RequestToDashScopeNativeClient() {
        OpenAiCompatibleLlmClient compatibleClient = mock(OpenAiCompatibleLlmClient.class);
        DashScopeNativeLlmClient nativeClient = mock(DashScopeNativeLlmClient.class);
        TraceService traceService = mock(TraceService.class);
        when(nativeClient.isEnabled()).thenReturn(true);
        when(nativeClient.modeLabel()).thenReturn("qwen:qwen3.7-flash");
        when(compatibleClient.modeLabel()).thenReturn("mock");
        when(nativeClient.complete(any(), any(), any())).thenReturn(Optional.of(
                new OpenAiCompatibleLlmClient.ChatCompletion<>(
                        "{\"skills\":[]}", new OpenAiCompatibleLlmClient.TokenUsage(3, 4, 7)
                )
        ));

        OpenAiCompatibleLlmAdapter adapter =
                new OpenAiCompatibleLlmAdapter(compatibleClient, nativeClient, traceService, new ObjectMapper());

        Optional<LlmResponse> response = adapter.complete(new LlmRequest(
                "qwen3.7-flash", List.of(
                        new LlmMessage(LlmMessage.Role.SYSTEM, "system"),
                        new LlmMessage(LlmMessage.Role.USER, "user")
                ), 0.1, 1800, 2_000L, "native-request", "resume-v1",
                LlmRequest.ResponseFormat.JSON_OBJECT, Map.of()
        ));

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().content()).isEqualTo("{\"skills\":[]}");
        verify(nativeClient).complete(any(), any(), any());
        verify(compatibleClient, never()).chatForJsonWithMetadata(any(), any(), any(), any());
    }
}
