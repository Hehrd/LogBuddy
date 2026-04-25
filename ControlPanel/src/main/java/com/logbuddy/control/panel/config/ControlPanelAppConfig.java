package com.logbuddy.control.panel.config;

public record ControlPanelAppConfig(ControlPanelGrpcConfig grpcSettings,
                                    Boolean isInK8sMode,
                                    ControlPanelSparkKubernetesConfig sparkK8sSettings) {
}
