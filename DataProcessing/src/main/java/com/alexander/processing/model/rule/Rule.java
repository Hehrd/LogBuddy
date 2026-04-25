package com.alexander.processing.model.rule;

import com.alexander.processing.model.rule.check.Check;
import lombok.AllArgsConstructor;

import java.util.List;

public record Rule(String ruleName, List<Check> checks, int logTargetCount, int maxCompletionsPerAlert) {
}
