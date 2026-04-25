package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.StringValueCheck;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StringValueCheckExecutor extends CheckExecutor<StringValueCheck> {

    @Override
    public Class<StringValueCheck> getCheckClass() {
        return StringValueCheck.class;
    }

    @Override
    public boolean executeCheck(StringValueCheck check, LogEntryDTO logEntry, ProcessingSession processingSession) {
        for (Map.Entry<String, StringValueCheck.StringValueInfo> fieldCheck : check.values().entrySet()) {
            String fieldValue = logEntry.fields().get(fieldCheck.getKey());
            if (fieldValue == null) {
                return false;
            }

            StringValueCheck.StringValueInfo valueInfo = fieldCheck.getValue();
            if (valueInfo.equalTo() != null && !valueInfo.equalTo().equals(fieldValue)) {
                return false;
            }
            if (valueInfo.notEqualTo() != null && valueInfo.notEqualTo().equals(fieldValue)) {
                return false;
            }
            if (valueInfo.longerThan() > 0 && fieldValue.length() <= valueInfo.longerThan()) {
                return false;
            }
            if (valueInfo.shorterThan() > 0 && fieldValue.length() >= valueInfo.shorterThan()) {
                return false;
            }
        }
        return true;
    }
}
