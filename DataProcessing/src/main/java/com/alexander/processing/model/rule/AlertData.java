package com.alexander.processing.model.rule;

import java.util.List;

public record AlertData(String alertName, List<String> requiredRules, Long timeWindowMillis,
                        List<String> alertEndpoints, Boolean aiOverviewEnabled) {
    @Override
    public List<String> requiredRules() {
        return List.copyOf(requiredRules);
    }
}
