package com.alexander.processing.data.service.rule;

import com.alexander.processing.data.model.log.LogFormat;
import com.alexander.processing.data.model.rule.ProcessingSession;
import com.alexander.processing.data.model.rule.check.Check;
import com.alexander.processing.data.model.rule.Rule;
import com.alexander.processing.data.service.alert.AlertingService;
import com.alexander.processing.data.service.rule.check.CheckExecutor;
import com.alexander.processing.ingest.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class RuleProcessingService {
    private final Map<Class<? extends Check>, CheckExecutor> checkExecutors;

    private final AlertingService alertingService;

    @Autowired
    public RuleProcessingService(AlertingService alertingService, ApplicationContext applicationContext) {
        this.alertingService = alertingService;
        this.checkExecutors = initExecutors(applicationContext);
    }

    public boolean processRule(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession) {
        Check check = rule.check();
        CheckExecutor checkExecutor = checkExecutors.get(check.getClass());
        return checkExecutor.executeCheck(rule, logEntry, logFormat, processingSession);
    }

    private Map<Class<? extends Check>, CheckExecutor> initExecutors(ApplicationContext applicationContext) {
        Collection<CheckExecutor> executors =
                applicationContext.getBeansOfType(CheckExecutor.class).values();
        Map<Class<? extends Check>, CheckExecutor> executorsMap = new HashMap<>();
        for (CheckExecutor executor : executors) {
            executorsMap.put(executor.getCheckClass(), executor);
        }
        return executorsMap;
    }
}
