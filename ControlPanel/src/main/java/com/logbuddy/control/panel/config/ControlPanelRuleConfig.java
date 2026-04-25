package com.logbuddy.control.panel.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

public record ControlPanelRuleConfig(Map<String, ControlPanelRuleEntry> rules) {
    @Override
    public Map<String, ControlPanelRuleEntry> rules() {
        return rules == null ? Map.of() : Map.copyOf(rules);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelRuleEntry(String ruleName,
                                        java.util.List<ControlPanelCheck> checks,
                                        Integer logTargetCount,
                                        Integer maxCompletionsPerAlert) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelCheck(String type) {
    }
}
