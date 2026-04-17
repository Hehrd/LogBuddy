package com.alexander.processing.model.rule;

import com.alexander.processing.model.alert.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
public class AlertSession {
    @Getter
    private String alertName;
    private Map<String, RuleCompletion> ruleCompletions;

    public AlertSession(AlertData alertData) {
        this.alertName = alertData.alertName();
        initRuleCompletions(alertData.requiredRules());
    }

    public List<RuleCompletion> getRuleCompletions() {
        return new ArrayList<>(ruleCompletions.values());
    }

    public Alert toAlert() {
        return new Alert(getRuleCompletions(), Instant.now());
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
