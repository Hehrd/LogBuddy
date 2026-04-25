package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.RegexMatchValueCheck;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RegexMatchValueCheckExecutor extends CheckExecutor<RegexMatchValueCheck> {

    @Override
    public Class<RegexMatchValueCheck> getCheckClass() {
        return RegexMatchValueCheck.class;
    }

    @Override
    public boolean executeCheck(RegexMatchValueCheck check, LogEntryDTO logEntry, ProcessingSession processingSession) {
        for (Map.Entry<String, RegexMatchValueCheck.RegexPatternInfo> fieldCheck : check.fields().entrySet()) {
            String fieldName = fieldCheck.getKey();
            RegexMatchValueCheck.RegexPatternInfo patternInfo = fieldCheck.getValue();

            String fieldValue = logEntry.fields().get(fieldName);
            if (fieldValue == null) {
                return false; // Field not present in log entry
            }

            if (patternInfo.matches() != null && !patternInfo.matches().matcher(fieldValue).matches()) {
                return false; // Does not match required pattern
            }
            if (patternInfo.notMatches() != null && patternInfo.notMatches().matcher(fieldValue).matches()) {
                return false; // Matches forbidden pattern
            }
        }
        return true;
    }
}
