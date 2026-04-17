package com.alexander.processing.service.rule;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.Check;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.service.alert.AlertingService;
import com.alexander.processing.service.rule.check.CheckExecutor;
import com.alexander.processing.ingest.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class RuleProcessingService {
    private static final Logger log = LoggerFactory.getLogger(RuleProcessingService.class);

    private final Map<Class<? extends Check>, CheckExecutor> checkExecutors;

    @Autowired
    public RuleProcessingService(AlertingService alertingService, ApplicationContext applicationContext) {
        this.checkExecutors = initExecutors(applicationContext);
    }

    public boolean processRule(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession) {
        Check check = rule.check();
        CheckExecutor checkExecutor = checkExecutors.get(check.getClass());
        log.debug("Executing rule {} with check {}", rule.ruleName(), check.getClass().getSimpleName());
        return checkExecutor.executeCheck(rule, logEntry, logFormat, processingSession);
    }

    private Map<Class<? extends Check>, CheckExecutor> initExecutors(ApplicationContext applicationContext) {
        Collection<CheckExecutor> executors =
                applicationContext.getBeansOfType(CheckExecutor.class).values();
        Map<Class<? extends Check>, CheckExecutor> executorsMap = new HashMap<>();
        for (CheckExecutor executor : executors) {
            executorsMap.put(executor.getCheckClass(), executor);
        }
        log.info("Initialized {} rule check executors", executorsMap.size());
        return executorsMap;
    }
}
