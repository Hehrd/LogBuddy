package com.alexander.processing.settings;

import lombok.AllArgsConstructor;

public record AppSettings(
        AppConfigurationSettings appConfigurationSettings,
        DataSourceSettings dataSourceSettings,
        RuleSettings ruleSettings) {
}

