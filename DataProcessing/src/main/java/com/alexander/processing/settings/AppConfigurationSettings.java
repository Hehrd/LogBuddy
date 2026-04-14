package com.alexander.processing.settings;


public record AppConfigurationSettings(int controlPanelServerPort, GrpcSettings grpcSettings) {
}
