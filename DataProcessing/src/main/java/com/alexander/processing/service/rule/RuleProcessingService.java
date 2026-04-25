package com.alexander.processing.service.rule;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.log.LogTraceSession;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.Check;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.model.rule.check.trace.TraceCheck;
import com.alexander.processing.service.rule.check.CheckExecutor;
import com.alexander.processing.service.rule.check.TraceCheckExecutor;
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
    private final Map<Class<? extends TraceCheck>, TraceCheckExecutor> traceCheckExecutors;

    @Autowired
    public RuleProcessingService(ApplicationContext applicationContext) {
        this.checkExecutors = initExecutors(applicationContext);
        this.traceCheckExecutors = initTraceExecutors(applicationContext);
    }

    public boolean processRule(Rule rule, LogEntryDTO logEntry, ProcessingSession processingSession) {
        for (Check check : rule.checks()) {
            if (check instanceof TraceCheck) {
                continue;
            }
            CheckExecutor checkExecutor = checkExecutors.get(check.getClass());
            if (checkExecutor == null) {
                log.warn("No executor found for check {}", check.getClass().getSimpleName());
                return false;
            }
            log.debug("Executing rule {} with check {}", rule.ruleName(), check.getClass().getSimpleName());
            if (!checkExecutor.executeCheck(check, logEntry, processingSession)) {
                return false;
            }
        }
        return true;
    }

    public boolean processTraceRule(Rule rule, LogTraceSession logTraceSession) {
        boolean hasTraceChecks = false;
        for (Check check : rule.checks()) {
            if (!(check instanceof TraceCheck traceCheck)) {
                continue;
            }
            hasTraceChecks = true;
            TraceCheckExecutor checkExecutor = traceCheckExecutors.get(traceCheck.getClass());
            if (checkExecutor == null) {
                log.warn("No trace executor found for check {}", traceCheck.getClass().getSimpleName());
                return false;
            }
            log.debug("Executing trace rule {} with check {}", rule.ruleName(), traceCheck.getClass().getSimpleName());
            if (!checkExecutor.executeCheck(traceCheck, logTraceSession)) {
                return false;
            }
        }
        return hasTraceChecks;
    }

    public boolean hasTraceChecks(Rule rule) {
        for (Check check : rule.checks()) {
            if (check instanceof TraceCheck) {
                return true;
            }
        }
        return false;
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

    private Map<Class<? extends TraceCheck>, TraceCheckExecutor> initTraceExecutors(ApplicationContext applicationContext) {
        Collection<TraceCheckExecutor> executors =
                applicationContext.getBeansOfType(TraceCheckExecutor.class).values();
        Map<Class<? extends TraceCheck>, TraceCheckExecutor> executorsMap = new HashMap<>();
        for (TraceCheckExecutor executor : executors) {
            executorsMap.put(executor.getCheckClass(), executor);
        }
        log.info("Initialized {} trace check executors", executorsMap.size());
        return executorsMap;
    }
}
