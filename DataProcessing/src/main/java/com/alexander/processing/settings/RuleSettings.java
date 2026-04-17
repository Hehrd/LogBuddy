package com.alexander.processing.settings;

import com.alexander.processing.model.rule.Rule;
import lombok.AllArgsConstructor;

import java.util.Map;

public record RuleSettings(Map<String, Rule> rules) {
    @Override
    public Map<String, Rule> rules() {
        return Map.copyOf(rules);
    }
}
