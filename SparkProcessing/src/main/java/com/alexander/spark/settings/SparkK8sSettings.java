package com.alexander.spark.settings;

public record SparkK8sSettings(String image,
                               String namespace,
                               String driverPodName,
                               String driverServiceName,
                               Integer executorInstances,
                               String executorMemory,
                               Integer executorCores,
                               String driverMemory,
                               Integer driverCores,
                               String serviceAccount) {
}
