package com.alexander.processing.model.rule;

import java.util.List;

public record AlertCondition(String alertName,
                             List<String> requiredRules,
                             Long timeWindowMillis,
                             List<String> alertEndpoints,
                             AlertConditionType alertConditionType,
                             Boolean aiOverviewEnabled) {
    @Override
    public List<String> requiredRules() {
        return List.copyOf(requiredRules);
    }
}
