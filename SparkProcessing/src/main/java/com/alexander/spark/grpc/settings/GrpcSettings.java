package com.alexander.spark.grpc.settings;

import java.io.Serializable;

public record GrpcSettings(String serverHost, int serverPort, int maxLinesPerReq) implements Serializable{
}
