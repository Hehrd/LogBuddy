package com.alexander.processing.data.model.rule;

import com.alexander.processing.data.model.rule.check.Check;
import lombok.AllArgsConstructor;

public record Rule(String ruleName, Check check, int logTargetCount, int maxCompletionsPerAlert) {
}
