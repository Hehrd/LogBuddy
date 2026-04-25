package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.log.LogTraceSession;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.ValueCheck;
import com.alexander.processing.model.rule.check.trace.FieldsChangeCheck;
import com.alexander.processing.model.rule.check.trace.TraceCheckStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FieldsChangeCheckExecutor extends TraceCheckExecutor<FieldsChangeCheck> {
    private final Map<Class<? extends ValueCheck>, CheckExecutor<? extends ValueCheck>> valueCheckExecutors;

    public FieldsChangeCheckExecutor(ApplicationContext applicationContext) {
        this.valueCheckExecutors = initExecutors(applicationContext.getBeansOfType(CheckExecutor.class).values());
    }

    @Override
    public Class<FieldsChangeCheck> getCheckClass() {
        return FieldsChangeCheck.class;
    }

    @Override
    public boolean executeCheck(FieldsChangeCheck check, LogTraceSession logTraceSession) {
        List<LogEntryDTO> logs = logTraceSession.getLogs();
        if (logs.size() < 2 || check.fields().isEmpty()) {
            return false;
        }

        for (int currentIndex = 1; currentIndex < logs.size(); currentIndex++) {
            LogEntryDTO currentLog = logs.get(currentIndex);
            if (matchesAnyReference(check, logs, currentIndex, currentLog)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean executeValueCheck(ValueCheck valueCheck, LogEntryDTO logEntry) {
        if (valueCheck == null) {
            return true;
        }
        CheckExecutor<ValueCheck> executor = (CheckExecutor<ValueCheck>) valueCheckExecutors.get(valueCheck.getClass());
        if (executor == null) {
            return false;
        }
        return executor.executeCheck(valueCheck, logEntry, (ProcessingSession) null);
    }

    private boolean matchesAnyReference(FieldsChangeCheck check,
                                        List<LogEntryDTO> logs,
                                        int currentIndex,
                                        LogEntryDTO currentLog) {
        TraceCheckStrategy strategy = check.strategy();
        switch (strategy) {
            case COMPARE_TO_PREVIOUS_EVENT -> {
                return matchesPair(check.fields(), logs.get(currentIndex - 1), currentLog);
            }
            case COMPARE_TO_FIRST_EVENT -> {
                return matchesPair(check.fields(), logs.getFirst(), currentLog);
            }
            case COMPARE_TO_ALL_PREVIOUS_EVENTS -> {
                for (int previousIndex = 0; previousIndex < currentIndex; previousIndex++) {
                    if (matchesPair(check.fields(), logs.get(previousIndex), currentLog)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private boolean matchesPair(Map<String, FieldsChangeCheck.FieldTransitionCheck> fieldChecks,
                                LogEntryDTO previousLog,
                                LogEntryDTO currentLog) {
        for (Map.Entry<String, FieldsChangeCheck.FieldTransitionCheck> fieldCheck : fieldChecks.entrySet()) {
            String fieldName = fieldCheck.getKey();
            FieldsChangeCheck.FieldTransitionCheck transitionCheck = fieldCheck.getValue();
            String previousValue = previousLog.fields().get(fieldName);
            String currentValue = currentLog.fields().get(fieldName);
            if (previousValue == null || currentValue == null) {
                return false;
            }
            boolean changed = !currentValue.equals(previousValue);
            if (transitionCheck.mode() == FieldsChangeCheck.FieldChangeMode.CHANGED && !changed) {
                return false;
            }
            if (transitionCheck.mode() == FieldsChangeCheck.FieldChangeMode.UNCHANGED && changed) {
                return false;
            }
            if (!executeValueCheck(transitionCheck.previousCheck(), previousLog)) {
                return false;
            }
            if (!executeValueCheck(transitionCheck.currentCheck(), currentLog)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<Class<? extends ValueCheck>, CheckExecutor<? extends ValueCheck>> initExecutors(Collection<CheckExecutor> executors) {
        Map<Class<? extends ValueCheck>, CheckExecutor<? extends ValueCheck>> executorsMap = new HashMap<>();
        for (CheckExecutor executor : executors) {
            if (ValueCheck.class.isAssignableFrom(executor.getCheckClass())) {
                executorsMap.put((Class<? extends ValueCheck>) executor.getCheckClass(), executor);
            }
        }
        return executorsMap;
    }
}
