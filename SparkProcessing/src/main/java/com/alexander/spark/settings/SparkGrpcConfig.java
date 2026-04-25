package com.alexander.spark.settings;

import java.io.Serializable;

public record SparkGrpcConfig(String serverHost, int maxLinesPerReq) implements Serializable{
}
