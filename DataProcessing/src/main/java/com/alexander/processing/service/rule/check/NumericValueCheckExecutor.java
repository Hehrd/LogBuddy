package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.NumericValueCheck;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NumericValueCheckExecutor extends CheckExecutor<NumericValueCheck> {

    @Override
    public Class<NumericValueCheck> getCheckClass() {
        return NumericValueCheck.class;
    }

    @Override
    public boolean executeCheck(NumericValueCheck check, LogEntryDTO logEntry, ProcessingSession processingSession) {
        for (Map.Entry<String, NumericValueCheck.NumericValueInfo> fieldCheck : check.values().entrySet()) {
            String fieldName = fieldCheck.getKey();
            NumericValueCheck.NumericValueInfo valueInfo = fieldCheck.getValue();
            long fieldValue;
            try {
                fieldValue = Long.parseLong(logEntry.fields().get(fieldName));
            } catch (Exception e) {
                return false; // Field not present or not a number
            }
            if (valueInfo.equalTo() != null && fieldValue != valueInfo.equalTo()) {
                return false;
            }
            if (valueInfo.notEqualTo() != null && fieldValue == valueInfo.lessThan()) {
                return false;
            }
            if (valueInfo.lessThan() != null && fieldValue > valueInfo.lessThan()) {
                return false;
            }
            if (valueInfo.moreThan() != null && fieldValue < valueInfo.moreThan()) {
                return false;
            }
            if (valueInfo.divisibleBy() != null && fieldValue % valueInfo.divisibleBy() != 0) {
                return false;
            }
        }
        return true;
    }
}
