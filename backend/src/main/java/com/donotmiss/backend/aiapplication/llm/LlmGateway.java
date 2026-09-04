package com.donotmiss.backend.aiapplication.llm;

import java.util.Optional;

/**
 * V2 application boundary. Its first adapter will delegate to the existing
 * OpenAiCompatibleLlmClient; that HTTP client remains the infrastructure.
 */
public interface LlmGateway {
    Optional<LlmResponse> complete(LlmRequest request);

    String modeLabel();
}
