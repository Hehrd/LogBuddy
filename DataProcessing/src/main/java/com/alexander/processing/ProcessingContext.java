package com.alexander.processing;

import com.alexander.processing.data.model.ds.DataSource;
import com.alexander.processing.data.model.rule.AlertData;
import com.alexander.processing.data.model.rule.AlertSession;
import com.alexander.processing.data.model.rule.ProcessingSession;
import com.alexander.processing.data.model.rule.RuleSession;
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

    private ProcessingSession createProcessingSession(DataSource ds) {
        Map<String, RuleSession> ruleSessions = new HashMap<>();
        for (AlertData alertData : ds.alertData().values()) {
            AlertSession alertSession = new AlertSession(alertData);
            for (String ruleName : alertData.requiredRules()) {
                if (ruleSessions.containsKey(ruleName)) {
                    RuleSession ruleSession = ruleSessions.get(ruleName);
                    ruleSession.addAlertSession(alertSession);
                    continue;
                }
                Map<String, AlertSession> alertSessions = new HashMap<>();
                alertSessions.put(alertSession.getAlertName(), alertSession);
                ruleSessions.put(ruleName, new RuleSession(ruleName,alertSessions));
            }
        }
        return new ProcessingSession(ruleSessions);
    }

}
