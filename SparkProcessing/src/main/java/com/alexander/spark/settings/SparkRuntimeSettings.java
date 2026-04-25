package com.alexander.spark.settings;

public record SparkRuntimeSettings(SparkAppConfig appConfigurationSettings,
                                   SparkDataSourceConfig dataSourceSettings) {
}
