package com.donotmiss.backend.aiapplication.trace;

import java.util.Map;

public record TraceSanitizationResult(
        Map<String, Object> sanitizedContent,
        boolean redacted,
        String contentHash,
        int droppedFieldCount
) {
}
