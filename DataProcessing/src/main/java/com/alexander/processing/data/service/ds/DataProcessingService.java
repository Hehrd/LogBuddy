package com.alexander.processing.data.service.ds;

import com.alexander.processing.ProcessingContext;
import com.alexander.processing.ingest.LogEntry;
import com.alexander.processing.settings.AppSettings;
import com.alexander.processing.data.model.alert.Alert;
import com.alexander.processing.data.model.ds.DataSource;
import com.alexander.processing.data.model.log.*;
import com.alexander.processing.data.model.rule.*;
import com.alexander.processing.data.service.alert.AlertingService;
import com.alexander.processing.data.service.rule.RuleProcessingService;
import com.alexander.processing.error.checked.LogParsingFailedException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class DataProcessingService {
    private final ProcessingContext processingContext;
    private final AppSettings appSettings;

    private final RuleProcessingService ruleProcessingService;
    private final AlertingService alertingService;

    public DataProcessingService(ProcessingContext processingContext, AppSettings appSettings,
                                 RuleProcessingService ruleProcessingService,
                                 AlertingService alertingService) {
        this.processingContext = processingContext;
        this.appSettings = appSettings;
        this.ruleProcessingService = ruleProcessingService;
        this.alertingService = alertingService;
    }



    @Async("dataProcessingExecutor")
    public void process(DataSource ds, List<LogEntry> logEntries) throws TimeoutException {
        processSingleLineLogs(ds, logEntries);
    }

    private void processSingleLineLogs(DataSource ds, List<LogEntry> logEntries) throws TimeoutException {
        for (LogEntry logEntry : logEntries) {
            processLogEntry(logEntry, ds);
        }
    }



    private void processLogEntry(LogEntry logEntry, DataSource ds) {
        ProcessingSession processingSession = processingContext.getProcessingSession(ds.name());
        for (String ruleName : ds.requiredRules()) {
            Rule rule = appSettings.ruleSettings().rules().get(ruleName);
            boolean isCheckSatisfied = ruleProcessingService.processRule(rule, logEntry, ds.logFormat(), processingSession);
            if (isCheckSatisfied) {
                handleCheckResult(ds, logEntry.getPlainText(), rule, processingSession);
            }
        }
    }

    private void handleCheckResult(DataSource ds, String log, Rule rule, ProcessingSession processingSession) {
        RuleSession ruleSession = processingSession.getRuleSession(rule.ruleName());
        ruleSession.addLog(log);
        if (ruleSession.getCurrentLogs().size() >= rule.logTargetCount()) {
            RuleCompletion newRuleCompletion = ruleSession.toRuleCompletion(Instant.now());
            for (AlertSession alertSession : ruleSession.getAlertSessions()) {
                alertSession.addRuleCompletion(newRuleCompletion);
                if (isAlertConditionSatisfied(alertSession)) {
                    sendAlert(alertSession, ds.alertData().get(alertSession.getAlertName()).alertEndpoints());
                    alertSession.flush();
                }
            }
            ruleSession.flush();
        }
    }

    private boolean isAlertConditionSatisfied(AlertSession alertSession) {
        for (RuleCompletion ruleCompletion : alertSession.getRuleCompletions()) {
            if (ruleCompletion == null) {
                return false;
            }
        }
        return true;
    }


    private void sendAlert(AlertSession alertSession, List<String> endpoints) {
        Alert alert = alertSession.toAlert();
        alertingService.sendAlert(endpoints, alert);
    }

}
