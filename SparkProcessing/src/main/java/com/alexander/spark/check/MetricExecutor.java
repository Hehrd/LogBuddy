package com.alexander.spark.check;

import com.alexander.spark.metric.StreamMetricSnapshot;
import org.apache.spark.util.LongAccumulator;

import java.io.Serializable;

public abstract class MetricExecutor<T extends StreamCheck> implements Serializable {
    private final String dataSourceName;
    private final T check;
    private final LongAccumulator accumulator;

    protected MetricExecutor(String dataSourceName, T check, LongAccumulator accumulator) {
        this.dataSourceName = dataSourceName;
        this.check = check;
        this.accumulator = accumulator;
    }

    public final void execute(MetricExecutionContext context) {
        if (matches(check, context)) {
            accumulator.add(1L);
        }
    }

    public final StreamMetricSnapshot snapshot(String publishedAt) {
        return new StreamMetricSnapshot(
                dataSourceName,
                metricKey(),
                metricName(),
                accumulator.value(),
                thresholdMillis(),
                publishedAt
        );
    }

    protected abstract boolean matches(T check, MetricExecutionContext context);

    protected abstract String metricKey();

    protected abstract String metricName();

    protected Long thresholdMillis() {
        return null;
    }
}
