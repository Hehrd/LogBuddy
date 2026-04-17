package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.model.rule.check.LogLevelCheck;
import com.alexander.processing.service.alert.AlertingService;
import com.alexander.processing.ingest.LogEntry;
import org.springframework.stereotype.Component;

@Component
public class LogLevelCheckExecutor extends CheckExecutor {

    public LogLevelCheckExecutor(AlertingService alertingService) {
        super(LogLevelCheck.class, alertingService);
    }

    @Override
    public boolean executeCheck(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession) {
        LogLevelCheck check = (LogLevelCheck) rule.check();
        String expectedLevel = check.level();
        String actualLevel = logEntry.getLevel();

        if (expectedLevel == null || actualLevel == null) {
            return false;
        }

        return actualLevel.equalsIgnoreCase(expectedLevel);
    }
}

