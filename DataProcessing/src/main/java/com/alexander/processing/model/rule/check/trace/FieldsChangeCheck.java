package com.alexander.processing.model.rule.check.trace;

import com.alexander.processing.model.rule.check.ValueCheck;

import java.util.Map;

public record FieldsChangeCheck(Map<String, FieldTransitionCheck> fields, TraceCheckStrategy strategy) implements TraceCheck {
    public record FieldTransitionCheck(FieldChangeMode mode,
                                       ValueCheck currentCheck,
                                       ValueCheck previousCheck) {
    }

    public enum FieldChangeMode {
        CHANGED,
        UNCHANGED
    }

    @Override
    public TraceCheckStrategy strategy() {
        return strategy;
    }
}
