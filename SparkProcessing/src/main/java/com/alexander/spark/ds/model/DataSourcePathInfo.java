package com.alexander.spark.ds.model;

import java.util.Map;

public record DataSourcePathInfo(Platform platform, String location, Map<String, String> options) {

}
