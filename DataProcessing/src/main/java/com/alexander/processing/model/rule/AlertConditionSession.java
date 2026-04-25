package com.alexander.processing.model.rule;

import com.alexander.processing.model.alert.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
public class AlertConditionSession {
    @Getter
    private String alertName;
    private AlertCondition alertCondition;
    private Map<String, RuleCompletion> ruleCompletions;

    public AlertConditionSession(AlertCondition alertCondition) {
        this.alertCondition = alertCondition;
        this.alertName = alertCondition.alertName();
        initRuleCompletions(alertCondition.requiredRules());
    }

    public List<RuleCompletion> getRuleCompletions() {
        return new ArrayList<>(ruleCompletions.values());
    }

    public Alert toAlert(String dataSourceName, String traceId) {
        List<RuleCompletion> completions = getRuleCompletions();
        Instant triggeredAt = Instant.now();
        Instant firstMatchedAt = completions.stream()
                .filter(Objects::nonNull)
                .map(RuleCompletion::getTimestamp)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(triggeredAt);
        Instant lastMatchedAt = completions.stream()
                .filter(Objects::nonNull)
                .map(RuleCompletion::getTimestamp)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(triggeredAt);
        List<String> sampleLogs = completions.stream()
                .filter(Objects::nonNull)
                .map(RuleCompletion::getLogs)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .distinct()
                .limit(10)
                .toList();

        return new Alert(
                UUID.randomUUID().toString(),
                alertName,
                alertCondition.alertConditionType(),
                dataSourceName,
                traceId,
                triggeredAt,
                firstMatchedAt,
                lastMatchedAt,
                alertCondition.timeWindowMillis(),
                alertCondition.requiredRules(),
                completions,
                sampleLogs,
                alertCondition.aiOverviewEnabled()
        );
    }

    public void addRuleCompletion(RuleCompletion ruleCompletion) {
        if(ruleCompletions.containsKey(ruleCompletion.getRuleName())) {
            ruleCompletions.put(ruleCompletion.getRuleName(), ruleCompletion);
        }
    }

    public void flush() {
       for (String ruleName : ruleCompletions.keySet()) {
           ruleCompletions.replace(ruleName, null);
       }
    }

    private void initRuleCompletions(List<String> requiredRules) {
        ruleCompletions = new HashMap<>();
        for(String ruleName : requiredRules) {
            ruleCompletions.put(ruleName, null);
        }
    }

}
