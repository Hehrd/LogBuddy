package com.logbuddy.control.panel.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

public record ControlPanelDataSourceConfig(Map<String, ControlPanelDataSourceEntry> dataSources,
                                           Long logTraceSessionTimeoutMillis) {
    @Override
    public Map<String, ControlPanelDataSourceEntry> dataSources() {
        return dataSources == null ? Map.of() : Map.copyOf(dataSources);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelDataSourceEntry(String name,
                                              ControlPanelPathInfo pathInfo,
                                              ControlPanelLogFormat logFormat,
                                              java.util.List<String> globalRequiredRules,
                                              java.util.List<String> traceRequiredRules,
                                              Map<String, ControlPanelAlertCondition> globalAlertConditions,
                                              Map<String, ControlPanelAlertCondition> traceAlertConditions,
                                              ControlPanelSchedule schedule) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelPathInfo(String location,
                                       String platform,
                                       Map<String, String> options) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelLogFormat(String logType,
                                        String keyValuePairRegex,
                                        String fullLogEntryRegex,
                                        String logEntryStartRegex,
                                        ControlPanelDefaultFields defaultFields,
                                        Map<String, String> customFields) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelDefaultFields(String timestamp,
                                            String timestampFormat,
                                            String level,
                                            String message,
                                            String source,
                                            String data,
                                            String logger) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelAlertCondition(String alertName,
                                             java.util.List<String> requiredRules,
                                             Long timeWindowMillis,
                                             java.util.List<String> alertEndpoints,
                                             String alertConditionType,
                                             Boolean aiOverviewEnabled) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControlPanelSchedule(Long delayAfterStartUpMillis,
                                       java.util.List<Long> intervalsMillis) {
    }
}
