package com.alexander.processing.model.rule;

import com.alexander.processing.model.log.LogTraceSession;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProcessingSession {
    private final Map<String, RuleSession> globalRuleSessions;
    private final Map<String, LogTraceSession> logTraces;
    private final Map<String, AlertCondition> traceAlertConditions;

    public ProcessingSession(Map<String, RuleSession> globalRuleSessions,
                             Map<String, LogTraceSession> logTraces,
                             Map<String, AlertCondition> traceAlertConditions) {
        this.globalRuleSessions = globalRuleSessions;
        this.logTraces = logTraces;
        this.traceAlertConditions = Map.copyOf(traceAlertConditions);
    }

    public RuleSession getGlobalRuleSession(String ruleName) {
        return globalRuleSessions.get(ruleName);
    }

    public boolean hasTraceAlerts() {
        return !traceAlertConditions.isEmpty();
    }

    public LogTraceSession getOrCreateLogTraceSession(String traceId) {
        return logTraces.computeIfAbsent(traceId, id -> new LogTraceSession(id, createRuleSessions(traceAlertConditions)));
    }

    public Collection<LogTraceSession> removeExpiredTraceSessions(Instant referenceTimestamp, long timeoutMillis) {
        Map<String, LogTraceSession> expiredSessions = new HashMap<>();
        for (Map.Entry<String, LogTraceSession> entry : logTraces.entrySet()) {
            if (entry.getValue().isExpired(referenceTimestamp, timeoutMillis)) {
                expiredSessions.put(entry.getKey(), entry.getValue());
            }
        }
        for (String traceId : expiredSessions.keySet()) {
            logTraces.remove(traceId);
        }
        return expiredSessions.values();
    }

    public int getTraceSessionCount() {
        return logTraces.size();
    }

    private Map<String, RuleSession> createRuleSessions(Map<String, AlertCondition> alertConditions) {
        Map<String, RuleSession> ruleSessions = new HashMap<>();
        for (AlertCondition alertCondition : alertConditions.values()) {
            AlertConditionSession alertConditionSession = new AlertConditionSession(alertCondition);
            for (String ruleName : alertCondition.requiredRules()) {
                RuleSession ruleSession = ruleSessions.computeIfAbsent(ruleName, ignored -> new RuleSession(ruleName, new HashMap<>()));
                ruleSession.addAlertSession(alertConditionSession);
            }
        }
        return ruleSessions;
    }
}
