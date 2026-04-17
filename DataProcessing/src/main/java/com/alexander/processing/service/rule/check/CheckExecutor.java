package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.Check;
import com.alexander.processing.service.alert.AlertingService;
import com.alexander.processing.ingest.LogEntry;
import lombok.Getter;

public abstract class CheckExecutor {
    protected AlertingService alertingService;

    @Getter
    protected Class<? extends Check> checkClass;

    protected CheckExecutor(Class<? extends Check> checkClass, AlertingService alertingService) {
        this.checkClass = checkClass;
        this.alertingService = alertingService;
    }


    public abstract boolean executeCheck(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession);


}
