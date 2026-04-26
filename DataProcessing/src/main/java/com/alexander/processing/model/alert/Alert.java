package com.alexander.processing.model.alert;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        Boolean aiOverviewEnabled,
        AiOverview aiOverview
) {
    public Alert {
        requiredRules = requiredRules == null ? List.of() : List.copyOf(requiredRules);
        completions = completions == null ? List.of() : List.copyOf(completions);
        sampleLogs = sampleLogs == null ? List.of() : List.copyOf(sampleLogs);
    }

    public record AiOverview(Boolean suspicious,
                             String severity,
                             @JsonProperty("attack_type") String attackType,
                             String summary,
                             List<String> evidence,
                             @JsonProperty("recommended_action") String recommendedAction,
                             Double confidence) {
    }
}
