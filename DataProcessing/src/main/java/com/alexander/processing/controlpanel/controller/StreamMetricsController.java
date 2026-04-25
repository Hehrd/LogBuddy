package com.alexander.processing.controlpanel.controller;

import com.alexander.processing.model.metric.StreamMetricsUpdate;
import com.alexander.processing.service.metric.StreamMetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
public class StreamMetricsController {
    private static final Logger log = LoggerFactory.getLogger(StreamMetricsController.class);

    private final StreamMetricService streamMetricService;

    public StreamMetricsController(StreamMetricService streamMetricService) {
        this.streamMetricService = streamMetricService;
    }

    @PostMapping("/stream")
    public ResponseEntity<Void> publishStreamMetrics(@RequestBody StreamMetricsUpdate metricsUpdate) {
        log.debug("Received {} stream metrics from SparkProcessing", metricsUpdate.metrics().size());
        streamMetricService.publish(metricsUpdate);
        return ResponseEntity.accepted().build();
    }
}
