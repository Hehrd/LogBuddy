package com.alexander.processing.model.rule.check;

import com.alexander.processing.model.rule.check.trace.TraceCheck;

import java.util.Map;

public record NumericValueCheck(Map<String, NumericValueInfo> values) implements ValueCheck {

   public record NumericValueInfo(Long lessThan,
                        Long moreThan,
                        Long equalTo,
                        Long notEqualTo,
                        Long divisibleBy) {}

}
