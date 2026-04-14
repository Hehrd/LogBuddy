package com.alexander.spark.settings;

public record AppSettings(AppConfigurationSettings appConfigurationSettings,
                          DataSourceSettings dataSourceSettings) {
}
