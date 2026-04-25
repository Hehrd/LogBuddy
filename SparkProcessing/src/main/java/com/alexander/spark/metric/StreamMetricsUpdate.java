package com.alexander.spark.metric;

import java.io.Serializable;
import java.util.List;

public record StreamMetricsUpdate(String publishedAt, List<StreamMetricSnapshot> metrics) implements Serializable {
}
