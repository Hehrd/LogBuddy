package com.alexander.processing.data.model.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleCompletion {
    private String ruleName;
    private Instant timestamp;
    private List<String> logs;
}
