package com.alexander.processing.model.dto;

import java.time.Instant;
import java.util.Map;

public record LogEntryDTO(
        String plainText,
        String traceId,
        String spanId,
        Instant timestamp,
        Map<String, String> fields
) {}
