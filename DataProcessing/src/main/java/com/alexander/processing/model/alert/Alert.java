package com.alexander.processing.model.alert;

import com.alexander.processing.model.rule.AlertConditionType;
import com.alexander.processing.model.rule.RuleCompletion;

import java.time.Instant;
import java.util.List;

public record Alert(
        String alertId,
        String alertName,
        AlertConditionType alertType,
        String dataSourceName,
        String traceId,
        Instant triggeredAt,
        Instant firstMatchedAt,
        Instant lastMatchedAt,
        Long timeWindowMillis,
        List<String> requiredRules,
        List<RuleCompletion> completions,
        List<String> sampleLogs,
        Boolean aiOverviewEnabled
) {
    public Alert {
        requiredRules = requiredRules == null ? List.of() : List.copyOf(requiredRules);
        completions = completions == null ? List.of() : List.copyOf(completions);
        sampleLogs = sampleLogs == null ? List.of() : List.copyOf(sampleLogs);
    }
}
