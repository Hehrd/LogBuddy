package com.alexander.spark.settings;

public record AppConfigurationSettings(GrpcSettings grpcSettings,
                                       Boolean isInK8sMode,
                                       SparkK8sSettings sparkK8sSettings) {
}
