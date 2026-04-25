package com.logbuddy.control.panel.config;

public record ControlPanelSparkKubernetesConfig(String image,
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
