package com.alexander.processing.model.metric;

import java.util.List;

public record StreamMetricsUpdate(String publishedAt, List<StreamMetric> metrics) {
    @Override
    public List<StreamMetric> metrics() {
        return metrics == null ? List.of() : List.copyOf(metrics);
    }
}
