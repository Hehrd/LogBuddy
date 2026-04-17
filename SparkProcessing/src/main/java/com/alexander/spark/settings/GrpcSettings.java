package com.alexander.spark.settings;

import java.io.Serializable;

public record GrpcSettings(String serverHost, int serverPort, int maxLinesPerReq) implements Serializable{
}
