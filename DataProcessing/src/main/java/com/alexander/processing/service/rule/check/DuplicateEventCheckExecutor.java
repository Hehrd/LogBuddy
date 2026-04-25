package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.log.LogTraceSession;
import com.alexander.processing.model.rule.check.trace.DuplicateEventCheck;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DuplicateEventCheckExecutor extends TraceCheckExecutor<DuplicateEventCheck> {

    @Override
    public Class<DuplicateEventCheck> getCheckClass() {
        return DuplicateEventCheck.class;
    }

    @Override
    public boolean executeCheck(DuplicateEventCheck check, LogTraceSession logTraceSession) {
        List<LogEntryDTO> logs = logTraceSession.getLogs();
        if (logs.size() < 2 || check.fields().isEmpty()) {
            return false;
        }

        for (int currentIndex = 1; currentIndex < logs.size(); currentIndex++) {
            LogEntryDTO currentLog = logs.get(currentIndex);
            switch (check.strategy()) {
                case COMPARE_TO_PREVIOUS_EVENT -> {
                    if (isDuplicate(check.fields(), currentLog, logs.get(currentIndex - 1))) {
                        return true;
                    }
                }
                case COMPARE_TO_FIRST_EVENT -> {
                    if (isDuplicate(check.fields(), currentLog, logs.getFirst())) {
                        return true;
                    }
                }
                case COMPARE_TO_ALL_PREVIOUS_EVENTS -> {
                    for (int previousIndex = 0; previousIndex < currentIndex; previousIndex++) {
                        if (isDuplicate(check.fields(), currentLog, logs.get(previousIndex))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isDuplicate(Map<String, String> configuredFields,
                                com.alexander.processing.model.dto.LogEntryDTO currentLog,
                                com.alexander.processing.model.dto.LogEntryDTO referenceLog) {
        for (Map.Entry<String, String> entry : configuredFields.entrySet()) {
            String fieldName = entry.getKey();
            String expectedValue = entry.getValue();
            String currentValue = currentLog.fields().get(fieldName);
            String referenceValue = referenceLog.fields().get(fieldName);
            if (currentValue == null || referenceValue == null) {
                return false;
            }
            if (expectedValue != null && !expectedValue.isBlank()) {
                if (!expectedValue.equals(currentValue) || !expectedValue.equals(referenceValue)) {
                    return false;
                }
                continue;
            }
            if (!currentValue.equals(referenceValue)) {
                return false;
            }
        }
        return true;
    }
}
