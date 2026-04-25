package com.alexander.processing.model.metric;

public record StreamMetric(String dataSourceName,
                           String metricKey,
                           String metricName,
                           long value,
                           Long thresholdMillis,
                           String publishedAt) {
}
