package com.alexander.spark.metric;

import java.io.Serializable;

public record StreamMetricSnapshot(String dataSourceName,
                                   String metricKey,
                                   String metricName,
                                   long value,
                                   Long thresholdMillis,
                                   String publishedAt) implements Serializable {
}
