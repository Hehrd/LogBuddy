package com.alexander.processing.data.model.rule;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleSession {
    @Getter
    private final String ruleName;
    private final Map<String, AlertSession> alertSessions;

    @Getter
    private List<String> currentLogs;


    public RuleSession(String ruleName, Map<String, AlertSession> alertSessions) {
        this.ruleName = ruleName;
        currentLogs = new ArrayList<>();
        this.alertSessions = alertSessions;
    }

    public void addAlertSession(AlertSession alertSession) {
        alertSessions.put(alertSession.getAlertName(), alertSession);
    }

    public List<AlertSession> getAlertSessions() {
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

