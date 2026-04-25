package com.alexander.spark.metric;

import com.alexander.spark.check.LogFormatCheck;
import com.alexander.spark.check.LogFormatMetricExecutor;
import com.alexander.spark.check.LogIngestionDelayCheck;
import com.alexander.spark.check.LogIngestionDelayMetricExecutor;
import com.alexander.spark.check.MetricExecutor;
import com.alexander.spark.check.StreamCheck;
import com.alexander.spark.ds.model.DataSource;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetricRegistry implements Serializable {
    private static final long DEFAULT_MAX_INGESTION_DELAY_MILLIS = 60000L;

    private final String metricsHost;
    private final Integer metricsPort;
    private final Map<String, List<MetricExecutor<? extends StreamCheck>>> metricExecutorsByDataSource;
    private final Map<String, Long> lastPublishedValues;

    public MetricRegistry(String metricsHost, Integer metricsPort) {
        this.metricsHost = metricsHost;
        this.metricsPort = metricsPort;
        this.metricExecutorsByDataSource = new ConcurrentHashMap<>();
        this.lastPublishedValues = new ConcurrentHashMap<>();
    }

    public void registerDataSource(DataSource dataSource, SparkSession sparkSession) {
        metricExecutorsByDataSource.computeIfAbsent(dataSource.getName(), ignored -> List.of(
                new LogFormatMetricExecutor(
                        dataSource.getName(),
                        new LogFormatCheck(),
                        sparkSession.sparkContext().longAccumulator(dataSource.getName() + ".parse_failures")
                ),
                new LogIngestionDelayMetricExecutor(
                        dataSource.getName(),
                        new LogIngestionDelayCheck(DEFAULT_MAX_INGESTION_DELAY_MILLIS),
                        sparkSession.sparkContext().longAccumulator(dataSource.getName() + ".delayed_ingestion")
                )
        ));
    }

    public List<MetricExecutor<? extends StreamCheck>> getExecutors(String dataSourceName) {
        return metricExecutorsByDataSource.getOrDefault(dataSourceName, List.of());
    }

    public void publishChangedMetrics() {
        String publishedAt = Instant.now().toString();
        List<StreamMetricSnapshot> changedMetrics = new ArrayList<>();
        for (List<MetricExecutor<? extends StreamCheck>> metricExecutors : metricExecutorsByDataSource.values()) {
            for (MetricExecutor<? extends StreamCheck> metricExecutor : metricExecutors) {
                StreamMetricSnapshot snapshot = metricExecutor.snapshot(publishedAt);
                String metricId = snapshot.dataSourceName() + ":" + snapshot.metricKey();
                Long previousValue = lastPublishedValues.get(metricId);
                if (previousValue != null && previousValue == snapshot.value()) {
                    continue;
                }
                lastPublishedValues.put(metricId, snapshot.value());
                changedMetrics.add(snapshot);
            }
        }
        if (!changedMetrics.isEmpty()) {
            MetricPublishClient.publish(metricsHost, metricsPort, new StreamMetricsUpdate(publishedAt, changedMetrics));
        }
    }

    public List<StreamMetricSnapshot> snapshots() {
        String publishedAt = Instant.now().toString();
        List<StreamMetricSnapshot> snapshots = new ArrayList<>();
        for (List<MetricExecutor<? extends StreamCheck>> metricExecutors : metricExecutorsByDataSource.values()) {
            for (MetricExecutor<? extends StreamCheck> metricExecutor : metricExecutors) {
                snapshots.add(metricExecutor.snapshot(publishedAt));
            }
        }
        return snapshots;
    }
}
