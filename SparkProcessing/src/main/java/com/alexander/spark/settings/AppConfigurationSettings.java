package com.alexander.spark.settings;

import com.alexander.spark.grpc.settings.GrpcSettings;

public record AppConfigurationSettings(int serverPort,
                                       GrpcSettings grpcSettings) {
}
