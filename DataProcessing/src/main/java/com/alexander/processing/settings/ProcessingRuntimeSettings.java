package com.alexander.processing.settings;

public record ProcessingRuntimeSettings(
        ProcessingAppConfig appConfigurationSettings,
        ProcessingDataSourceConfig dataSourceSettings,
        ProcessingRuleConfig ruleSettings) {
}
