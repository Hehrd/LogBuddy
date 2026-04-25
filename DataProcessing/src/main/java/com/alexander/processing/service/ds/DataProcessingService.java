package com.alexander.processing.service.ds;

import com.alexander.processing.context.ProcessingContext;
import com.alexander.processing.model.alert.Alert;
import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.log.LogTraceSession;
import com.alexander.processing.model.ds.DataSource;
import com.alexander.processing.model.rule.AlertCondition;
import com.alexander.processing.model.rule.AlertConditionSession;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.model.rule.RuleCompletion;
import com.alexander.processing.model.rule.RuleSession;
import com.alexander.processing.service.alert.AlertingService;
import com.alexander.processing.service.rule.RuleProcessingService;
import com.alexander.processing.settings.ProcessingRuntimeSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class DataProcessingService {
    private static final Logger log = LoggerFactory.getLogger(DataProcessingService.class);

    private final ProcessingContext processingContext;
    private final ProcessingRuntimeSettings appSettings;

    private final RuleProcessingService ruleProcessingService;
    private final AlertingService alertingService;

    public DataProcessingService(ProcessingContext processingContext, ProcessingRuntimeSettings appSettings,
                                 RuleProcessingService ruleProcessingService,
                                 AlertingService alertingService) {
        this.processingContext = processingContext;
        this.appSettings = appSettings;
        this.ruleProcessingService = ruleProcessingService;
        this.alertingService = alertingService;
    }

    @Async("dataProcessingExecutor")
    public void process(DataSource ds, List<LogEntryDTO> logEntries) {
        log.info("Processing {} log entries for data source {}", logEntries.size(), ds.name());
        processSingleLineLogs(ds, logEntries);
        log.debug("Finished processing batch for data source {}", ds.name());
    }

    private void processSingleLineLogs(DataSource ds, List<LogEntryDTO> logEntries) {
        for (LogEntryDTO logEntry : logEntries) {
            processLogEntry(logEntry, ds);
        }
    }

    private void processLogEntry(LogEntryDTO logEntry, DataSource ds) {
        ProcessingSession processingSession = processingContext.getProcessingSession(ds.name());
        LogTraceSession logTraceSession = getLogTraceSession(processingSession, logEntry);
        for (String ruleName : ds.globalRequiredRules()) {
            Rule rule = appSettings.ruleSettings().rules().get(ruleName);
            if (rule != null && ruleProcessingService.processRule(rule, logEntry, processingSession)) {
                handleCheckResult(
                        ds.globalAlertConditions(),
                        ds.name(),
                        logEntry.plainText(),
                        rule,
                        processingSession.getGlobalRuleSession(rule.ruleName()),
                        logEntry.timestamp());
            }
        }

        if (logTraceSession == null) {
            return;
        }

        for (String ruleName : ds.traceRequiredRules()) {
            Rule rule = appSettings.ruleSettings().rules().get(ruleName);
            if (rule != null && ruleProcessingService.processRule(rule, logEntry, processingSession)) {
                handleCheckResult(
                        ds.traceAlertConditions(),
                        ds.name(),
                        logEntry.plainText(),
                        rule,
                        logTraceSession.getRuleSession(rule.ruleName()),
                        logEntry.timestamp());
            }
        }
    }

    private void handleCheckResult(Map<String, AlertCondition> alertConditions,
                                   String dataSourceName,
                                   String logLine,
                                   Rule rule,
                                   RuleSession ruleSession,
                                   Instant logTimestamp) {
        if (ruleSession == null) {
            return;
        }
        ruleSession.addLog(logLine);
        if (ruleSession.getCurrentLogs().size() >= rule.logTargetCount()) {
            log.info("Rule {} reached target count for data source {}", rule.ruleName(), dataSourceName);
            RuleCompletion newRuleCompletion = ruleSession.toRuleCompletion(logTimestamp);
            for (AlertConditionSession alertConditionSession : ruleSession.getAlertSessions()) {
                alertConditionSession.addRuleCompletion(newRuleCompletion);
                if (isAlertConditionSatisfied(alertConditionSession)) {
                    log.info("Alert {} satisfied for data source {}", alertConditionSession.getAlertName(), dataSourceName);
                    sendAlert(alertConditionSession, dataSourceName, null,
                            alertConditions.get(alertConditionSession.getAlertName()).alertEndpoints());
                    alertConditionSession.flush();
                }
            }
            ruleSession.flush();
        }
    }

    private boolean isAlertConditionSatisfied(AlertConditionSession alertConditionSession) {
        for (RuleCompletion ruleCompletion : alertConditionSession.getRuleCompletions()) {
            if (ruleCompletion == null) {
                return false;
            }
        }
        return true;
    }


    private void sendAlert(AlertConditionSession alertConditionSession,
                           String dataSourceName,
                           String traceId,
                           List<String> endpoints) {
        Alert alert = alertConditionSession.toAlert(dataSourceName, traceId);
        alertingService.sendAlert(endpoints, alert);
    }

    private LogTraceSession getLogTraceSession(ProcessingSession processingSession, LogEntryDTO logEntry) {
        if (!processingSession.hasTraceAlerts()) {
            return null;
        }
        if (logEntry.traceId() == null || logEntry.traceId().isBlank()) {
            log.debug("Skipping trace-scoped processing for log without traceId");
            return null;
        }
        LogTraceSession logTraceSession = processingSession.getOrCreateLogTraceSession(logEntry.traceId());
        logTraceSession.addLog(logEntry);
        return logTraceSession;
    }

    @Scheduled(fixedDelay = 60000)
    public void clearExpiredTraceSessions() {
        long timeoutMillis = appSettings.dataSourceSettings().logTraceSessionTimeoutMillis();
        Instant now = Instant.now();
        for (Map.Entry<String, ProcessingSession> entry : processingContext.getProcessingSessions().entrySet()) {
            DataSource dataSource = appSettings.dataSourceSettings().dataSources().get(entry.getKey());
            List<LogTraceSession> expiredTraceSessions = entry.getValue().removeExpiredTraceSessions(now, timeoutMillis)
                    .stream()
                    .toList();
            processExpiredTraceSessions(dataSource, expiredTraceSessions);
            List<String> expiredTraceIds = expiredTraceSessions.stream()
                    .map(LogTraceSession::getTraceId)
                    .toList();
            if (!expiredTraceIds.isEmpty()) {
                log.info("Removed {} expired trace sessions for data source {}: {}", expiredTraceIds.size(), entry.getKey(), expiredTraceIds);
            }
        }
    }

    private void processExpiredTraceSessions(DataSource ds, List<LogTraceSession> expiredTraceSessions) {
        if (ds == null || expiredTraceSessions.isEmpty()) {
            return;
        }

        for (LogTraceSession traceSession : expiredTraceSessions) {
            for (String ruleName : ds.traceRequiredRules()) {
                Rule rule = appSettings.ruleSettings().rules().get(ruleName);
                if (rule == null) {
                    continue;
                }
                if (ruleProcessingService.processTraceRule(rule, traceSession)) {
                    handleTraceCheckResult(ds, rule, traceSession);
                }
            }
        }
    }

    private void handleTraceCheckResult(DataSource ds, Rule rule, LogTraceSession logTraceSession) {
        RuleSession ruleSession = logTraceSession.getRuleSession(rule.ruleName());
        if (ruleSession == null) {
            return;
        }

        for (LogEntryDTO traceLog : logTraceSession.getLogs()) {
            ruleSession.addLog(traceLog.plainText());
        }

        Instant completionTimestamp = logTraceSession.getLogs().isEmpty()
                ? Instant.now()
                : logTraceSession.getLogs().getLast().timestamp();
        if (ruleSession.getCurrentLogs().size() < rule.logTargetCount()) {
            return;
        }

        log.info("Trace rule {} reached target count for data source {}", rule.ruleName(), ds.name());
        RuleCompletion newRuleCompletion = ruleSession.toRuleCompletion(completionTimestamp);
        for (AlertConditionSession alertConditionSession : ruleSession.getAlertSessions()) {
            alertConditionSession.addRuleCompletion(newRuleCompletion);
            if (isAlertConditionSatisfied(alertConditionSession)) {
                log.info("Alert {} satisfied for data source {}", alertConditionSession.getAlertName(), ds.name());
                sendAlert(
                        alertConditionSession,
                        ds.name(),
                        logTraceSession.getTraceId(),
                        ds.traceAlertConditions().get(alertConditionSession.getAlertName()).alertEndpoints()
                );
                alertConditionSession.flush();
            }
        }
        ruleSession.flush();
    }

}
