package com.alexander.processing.data.model.rule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessingSession {
    private final Map<String, RuleSession> ruleSessions;

    public ProcessingSession(Map<String, RuleSession> ruleSessions) {
        this.ruleSessions = ruleSessions;
    }

    public RuleSession getRuleSession(String ruleName) {
        return ruleSessions.get(ruleName);
    }

}
