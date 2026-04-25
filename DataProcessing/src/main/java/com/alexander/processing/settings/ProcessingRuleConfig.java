package com.alexander.processing.settings;

import com.alexander.processing.model.rule.Rule;

import java.util.Map;

public record ProcessingRuleConfig(Map<String, Rule> rules) {
    @Override
    public Map<String, Rule> rules() {
        return Map.copyOf(rules);
    }
}
