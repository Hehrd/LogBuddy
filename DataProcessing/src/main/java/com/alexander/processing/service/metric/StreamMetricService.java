package com.alexander.processing.service.metric;

import com.alexander.processing.config.AlertWebSocketConfig;
import com.alexander.processing.model.metric.StreamMetric;
import com.alexander.processing.model.metric.StreamMetricsUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StreamMetricService {
    private static final Logger log = LoggerFactory.getLogger(StreamMetricService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, StreamMetric> latestMetrics = new LinkedHashMap<>();

    public StreamMetricService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(StreamMetricsUpdate metricsUpdate) {
        if (metricsUpdate == null || metricsUpdate.metrics().isEmpty()) {
            return;
        }
        for (StreamMetric metric : metricsUpdate.metrics()) {
            latestMetrics.put(metric.dataSourceName() + ":" + metric.metricKey(), metric);
        }
        log.debug("Publishing {} stream metrics to dashboard topic", metricsUpdate.metrics().size());
        messagingTemplate.convertAndSend(AlertWebSocketConfig.STREAM_METRICS_TOPIC, metricsUpdate);
    }

    public List<StreamMetric> latestMetrics() {
        return List.copyOf(latestMetrics.values());
    }
}
