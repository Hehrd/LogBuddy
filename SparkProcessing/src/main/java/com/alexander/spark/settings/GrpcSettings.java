package com.alexander.spark.settings;

import java.io.Serializable;

public record GrpcSettings(String serverHost, int maxLinesPerReq) implements Serializable{
}
