package com.alexander.processing.model.log;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.rule.RuleSession;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class LogTraceSession {
    private final String traceId;

    private final List<LogEntryDTO> logs;
    private final Map<String, RuleSession> ruleSessions;
    private Instant traceStart;
    private Instant lastUpdate;

    public LogTraceSession(String traceId, Map<String, RuleSession> ruleSessions) {
        this.traceId = traceId;
        this.ruleSessions = ruleSessions;
        this.logs = new ArrayList<>();
    }

    public void addLog(LogEntryDTO logEntry) {
        logs.add(logEntry);
        Instant logTimestamp = logEntry.timestamp();
        if (traceStart == null || logTimestamp.isBefore(traceStart)) {
            traceStart = logTimestamp;
        }
        lastUpdate = Instant.now();
    }

    public List<LogEntryDTO> getLogs() {
        return List.copyOf(logs);
    }
    public Map<String, RuleSession> getRuleSessions() {
        return Map.copyOf(ruleSessions);
    }

    public RuleSession getRuleSession(String ruleName) {
        return ruleSessions.get(ruleName);
    }

    public boolean isExpired(Instant referenceTimestamp, long timeoutMillis) {
        if (lastUpdate == null) {
            return false;
        }
        return lastUpdate.plusMillis(timeoutMillis).isBefore(referenceTimestamp);
    }
}
