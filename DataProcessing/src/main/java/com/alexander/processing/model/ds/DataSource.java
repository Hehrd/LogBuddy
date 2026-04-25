package com.alexander.processing.model.ds;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.AlertCondition;

import java.util.List;
import java.util.Map;

public record DataSource(String name,
                         String path,
                         LogFormat logFormat,
                         List<String> globalRequiredRules,
                         List<String> traceRequiredRules,
                         Map<String, AlertCondition> globalAlertConditions,
                         Map<String, AlertCondition> traceAlertConditions,
                         DataSourceSchedule schedule) {
    @Override
    public Map<String, AlertCondition> globalAlertConditions() {
        return Map.copyOf(globalAlertConditions);
    }

    @Override
    public Map<String, AlertCondition> traceAlertConditions() {
        return Map.copyOf(traceAlertConditions);
    }

    @Override
    public List<String> globalRequiredRules() {
        return List.copyOf(globalRequiredRules);
    }

    @Override
    public List<String> traceRequiredRules() {
        return List.copyOf(traceRequiredRules);
    }
}
