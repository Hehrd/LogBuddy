package com.alexander.processing.model.rule;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleSession {
    @Getter
    private final String ruleName;
    private final Map<String, AlertConditionSession> alertSessions;

    @Getter
    private List<String> currentLogs;


    public RuleSession(String ruleName, Map<String, AlertConditionSession> alertSessions) {
        this.ruleName = ruleName;
        currentLogs = new ArrayList<>();
        this.alertSessions = alertSessions;
    }

    public void addAlertSession(AlertConditionSession alertConditionSession) {
        alertSessions.put(alertConditionSession.getAlertName(), alertConditionSession);
    }

    public List<AlertConditionSession> getAlertSessions() {
        return new ArrayList<>(alertSessions.values());
    }

    public RuleCompletion toRuleCompletion(Instant timestamp) {
        return new RuleCompletion(ruleName, timestamp, currentLogs);
    }

    public void flush() {
        currentLogs = new ArrayList<>();
    }

    public void addLog(String log) {
        currentLogs.add(log);
    }
}

