package com.alexander.processing.model.rule.check.trace;

import java.util.Map;

public record DuplicateEventCheck(Map<String, String> fields, TraceCheckStrategy strategy) implements TraceCheck{
    @Override
    public TraceCheckStrategy strategy() {
        return strategy;
    }
}
