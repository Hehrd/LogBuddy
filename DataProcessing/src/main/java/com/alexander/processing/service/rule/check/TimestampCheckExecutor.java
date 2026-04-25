package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.TimestampValueCheck;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class TimestampCheckExecutor extends CheckExecutor<TimestampValueCheck> {

    @Override
    public Class<TimestampValueCheck> getCheckClass() {
        return TimestampValueCheck.class;
    }

    @Override
    public boolean executeCheck(TimestampValueCheck check, LogEntryDTO logEntry, ProcessingSession processingSession) {
        for (Map.Entry<String, TimestampValueCheck.TimestampValueInfo> fieldCheck : check.values().entrySet()) {
            String fieldName = fieldCheck.getKey();
            TimestampValueCheck.TimestampValueInfo valueInfo = fieldCheck.getValue();

            Instant fieldValue;
            try {
                fieldValue = Instant.parse(logEntry.fields().get(fieldName));
            } catch (Exception e) {
                return false; // Field value is not a valid timestamp
            }

            if (valueInfo.before() != null && !fieldValue.isBefore(valueInfo.before())) {
                return false; // Timestamp is not before the specified time
            }
            if (valueInfo.after() != null && !fieldValue.isAfter(valueInfo.after())) {
                return false; // Timestamp is not after the specified time
            }
        }

        return true;
    }
}
