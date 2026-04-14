package com.alexander.processing.data.model.alert;


import com.alexander.processing.data.model.rule.RuleCompletion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class Alert {
    private List<RuleCompletion> data;
    private Instant timestamp;
}
