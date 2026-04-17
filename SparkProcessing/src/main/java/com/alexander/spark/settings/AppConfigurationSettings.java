package com.alexander.spark.settings;

public record AppConfigurationSettings(int serverPort,
                                       GrpcSettings grpcSettings,
                                       Boolean isInK8sMode,
                                       SparkK8sSettings sparkK8sSettings) {
}
