package com.alexander.spark.settings;

public record SparkAppConfig(SparkGrpcConfig grpcSettings,
                             Boolean isInK8sMode,
                             SparkKubernetesConfig sparkK8sSettings) {
}
