package com.alexander.processing.service.ds;

import com.alexander.processing.context.ProcessingContext;
import com.alexander.processing.ingest.LogEntry;
import com.alexander.processing.model.alert.Alert;
import com.alexander.processing.model.ds.DataSource;
import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.AlertSession;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.model.rule.RuleCompletion;
import com.alexander.processing.model.rule.RuleSession;
import com.alexander.processing.service.alert.AlertingService;
import com.alexander.processing.service.rule.RuleProcessingService;
import com.alexander.processing.settings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DataProcessingService {
    private static final Logger log = LoggerFactory.getLogger(DataProcessingService.class);

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
    public void process(DataSource ds, List<LogEntry> logEntries) {
        log.info("Processing {} log entries for data source {}", logEntries.size(), ds.name());
        processSingleLineLogs(ds, logEntries);
        log.debug("Finished processing batch for data source {}", ds.name());
    }

    private void processSingleLineLogs(DataSource ds, List<LogEntry> logEntries) {
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

    private void handleCheckResult(DataSource ds, String logLine, Rule rule, ProcessingSession processingSession) {
        RuleSession ruleSession = processingSession.getRuleSession(rule.ruleName());
        ruleSession.addLog(logLine);
        if (ruleSession.getCurrentLogs().size() >= rule.logTargetCount()) {
            log.info("Rule {} reached target count for data source {}", rule.ruleName(), ds.name());
            RuleCompletion newRuleCompletion = ruleSession.toRuleCompletion(Instant.now());
            for (AlertSession alertSession : ruleSession.getAlertSessions()) {
                alertSession.addRuleCompletion(newRuleCompletion);
                if (isAlertConditionSatisfied(alertSession)) {
                    log.info("Alert {} satisfied for data source {}", alertSession.getAlertName(), ds.name());
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
