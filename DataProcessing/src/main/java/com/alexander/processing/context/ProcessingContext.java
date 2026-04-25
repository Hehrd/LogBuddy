package com.alexander.processing.context;

import com.alexander.processing.model.ds.DataSource;
import com.alexander.processing.model.rule.AlertCondition;
import com.alexander.processing.model.rule.AlertConditionSession;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.RuleSession;
import com.alexander.processing.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessingContext {
    private final Map<String, ProcessingSession> processingSessions = new HashMap<>();

    @Autowired
    public ProcessingContext(AppSettings appSettings) {
        Collection<DataSource> dataSources = appSettings.dataSourceSettings().dataSources().values();
        for (DataSource dataSource : dataSources) {
            ProcessingSession processingSession = createProcessingSession(dataSource);
            processingSessions.put(dataSource.name(), processingSession);
        }
    }

    public ProcessingSession getProcessingSession(String dataSourceName) {
        return processingSessions.get(dataSourceName);
    }

    public Map<String, ProcessingSession> getProcessingSessions() {
        return Map.copyOf(processingSessions);
    }

    private ProcessingSession createProcessingSession(DataSource ds) {
        return new ProcessingSession(
                createRuleSessions(ds.globalAlertConditions()),
                new HashMap<>(),
                ds.traceAlertConditions());
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
