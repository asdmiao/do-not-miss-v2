package com.donotmiss.backend.aiapplication.trace;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Conservative default: allow operational metadata and drop unrecognized content fields. */
@Component
public class DefaultTraceSanitizer implements TraceSanitizer {
    private static final Set<String> ALLOWED = Set.of(
            "requestid", "agentname", "model", "promptversion", "latencymillis", "usage",
            "status", "toolname", "inputhash", "sanitizedinput", "outputsummary", "error", "runid", "stepid",
            "action", "state", "skillcode", "documentid", "sourceversionhash", "jobversionhash", "queryhash",
            "chunkid", "section", "requirementcount", "chunkcount", "evidencecount", "requirementid", "evidenceid",
            "evidencestrength", "confidence", "score", "rank", "count", "mode"
    );
    private static final Set<String> MASKED = Set.of("userid", "sourceid", "email", "phone", "address", "name");
    private static final Set<String> HASHED = Set.of("resumeid", "interviewid", "projectid", "candidateid");

    @Override
    public TraceSanitizationResult sanitize(String artifactType, Map<String, Object> rawContent) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        int dropped = 0;
        if (rawContent != null) {
            for (Map.Entry<String, Object> entry : rawContent.entrySet()) {
                TraceDataClassification classification = classify(entry.getKey());
                if (classification == TraceDataClassification.ALLOW) {
                    if (entry.getValue() != null) {
                        sanitized.put(entry.getKey(), safeAllowedValue(entry.getKey(), entry.getValue()));
                    }
                } else if (classification == TraceDataClassification.MASK) {
                    sanitized.put(entry.getKey(), "***");
                } else if (classification == TraceDataClassification.HASH_ONLY) {
                    sanitized.put(entry.getKey() + "Hash", hash(String.valueOf(entry.getValue())));
                } else {
                    dropped += 1;
                }
            }
        }
        sanitized.put("artifactType", artifactType == null ? "unknown" : artifactType);
        return new TraceSanitizationResult(Map.copyOf(sanitized), true, hash(String.valueOf(sanitized)), dropped);
    }

    @Override
    public TraceDataClassification classify(String fieldName) {
        String normalized = fieldName == null ? "" : fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        if (normalized.contains("prompt") || normalized.contains("response") || normalized.contains("content")
                || normalized.contains("text") || normalized.contains("answer") || normalized.contains("resume")
                || normalized.contains("token") || normalized.contains("secret") || normalized.contains("apikey")) {
            return TraceDataClassification.DROP;
        }
        if ("outputsummary".equals(normalized)) {
            return TraceDataClassification.HASH_ONLY;
        }
        if ("error".equals(normalized)) {
            return TraceDataClassification.MASK;
        }
        if (MASKED.contains(normalized)) {
            return TraceDataClassification.MASK;
        }
        if (HASHED.contains(normalized)) {
            return TraceDataClassification.HASH_ONLY;
        }
        return ALLOWED.contains(normalized) ? TraceDataClassification.ALLOW : TraceDataClassification.DROP;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private Object safeAllowedValue(String fieldName, Object value) {
        String normalized = fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        if ("sanitizedinput".equals(normalized)) {
            String text = String.valueOf(value);
            return text.startsWith("argumentKeys=") ? text : "***";
        }
        if (!"usage".equals(normalized) || !(value instanceof Map<?, ?> source)) {
            return value;
        }
        Map<String, Object> usage = new LinkedHashMap<>();
        copyNumber(source, usage, "promptTokens");
        copyNumber(source, usage, "completionTokens");
        copyNumber(source, usage, "totalTokens");
        Object available = source.get("available");
        if (available instanceof Boolean) {
            usage.put("available", available);
        }
        return Map.copyOf(usage);
    }

    private void copyNumber(Map<?, ?> source, Map<String, Object> target, String key) {
        Object value = source.get(key);
        if (value instanceof Number) {
            target.put(key, value);
        }
    }
}
