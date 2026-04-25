package com.alexander.processing.model.rule.check;

import java.time.Instant;
import java.util.Map;

public record TimestampValueCheck(Map<String, TimestampValueInfo> values) implements ValueCheck {
    public record TimestampValueInfo(Instant before, Instant after) { }
}
