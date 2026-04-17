package com.alexander.processing.model.ds;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.AlertData;
import lombok.*;

import java.util.List;
import java.util.Map;

public record DataSource(String name, String path, LogFormat logFormat, List<String> requiredRules,
                         Map<String, AlertData> alertData, DataSourceSchedule schedule) {
    @Override
    public Map<String, AlertData> alertData() {
        return Map.copyOf(alertData);
    }

    @Override
    public List<String> requiredRules() {
        return List.copyOf(requiredRules);
    }
}
