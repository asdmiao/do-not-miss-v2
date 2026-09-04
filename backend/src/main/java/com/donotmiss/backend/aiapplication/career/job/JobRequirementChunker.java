package com.donotmiss.backend.aiapplication.career.job;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/** Deterministic, section-aware plain-text chunking. It deliberately does not attempt document/OCR parsing. */
@Component
public class JobRequirementChunker {
    private static final int MAX_CHARS = 1400;

    public List<ChunkDraft> chunk(String content) {
        if (content == null || content.isBlank()) return List.of();
        List<ChunkDraft> result = new ArrayList<>();
        JobRequirementChunkSection section = JobRequirementChunkSection.GENERAL;
        StringBuilder current = new StringBuilder();
        int position = 0;
        for (String rawLine : content.replace("\r", "").split("\n")) {
            String line = rawLine.trim();
            if (line.isBlank()) continue;
            JobRequirementChunkSection heading = headingSection(line);
            if (heading != null) {
                position = flush(result, current, section, position);
                section = heading;
                continue;
            }
            if (current.length() > 0 && current.length() + line.length() + 1 > MAX_CHARS) {
                position = flush(result, current, section, position);
            }
            if (current.length() > 0) current.append('\n');
            current.append(line);
        }
        flush(result, current, section, position);
        return List.copyOf(result);
    }

    private int flush(List<ChunkDraft> target, StringBuilder current, JobRequirementChunkSection section, int position) {
        String value = current.toString().trim(); current.setLength(0);
        if (value.isBlank()) return position;
        int next = position + 1;
        target.add(new ChunkDraft(section, value, next, sha256(value)));
        return next;
    }

    private JobRequirementChunkSection headingSection(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[：:()（）\\[\\]#*\\- ]", "");
        if (normalized.matches(".*(岗位职责|工作职责|职责|responsibilities|whatyouwilldo).*")) return JobRequirementChunkSection.RESPONSIBILITIES;
        if (normalized.matches(".*(任职要求|职位要求|岗位要求|任职资格|requirements|qualifications).*")) return JobRequirementChunkSection.REQUIREMENTS;
        if (normalized.matches(".*(技术栈|技术要求|techstack|technology).*")) return JobRequirementChunkSection.TECH_STACK;
        if (normalized.matches(".*(加分项|优先条件|preferred|nicetohave|plus).*")) return JobRequirementChunkSection.PREFERRED;
        return null;
    }

    private String sha256(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); } }
    public record ChunkDraft(JobRequirementChunkSection section, String content, int position, String contentHash) { }
}
