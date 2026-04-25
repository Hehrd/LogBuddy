package com.alexander.processing.controlpanel.controller;

import com.alexander.processing.controlpanel.service.ControlPanelService;
import com.alexander.processing.service.metric.StreamMetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/control-panel")
public class ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(ControlPanelController.class);

    private final ControlPanelService controlPanelService;
    private final StreamMetricService streamMetricService;

    public ControlPanelController(ControlPanelService controlPanelService,
                                  StreamMetricService streamMetricService) {
        this.controlPanelService = controlPanelService;
        this.streamMetricService = streamMetricService;
    }

    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        log.debug("Received health check request");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.debug("Received status check request");
        return ResponseEntity.ok(controlPanelService.status());
    }

    @PostMapping("/sleep")
    public ResponseEntity<Void> sleep() {
        log.info("Pausing data source ingest via control panel");
        controlPanelService.sleep();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/wake")
    public ResponseEntity<Void> wake() {
        log.info("Resuming data source ingest via control panel");
        controlPanelService.wake();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restart")
    public ResponseEntity<Void> restart() {
        log.info("Restart requested via control panel");
        controlPanelService.restart();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Void> shutdown() {
        log.info("Shutdown requested via control panel");
        controlPanelService.shutdown();
        return ResponseEntity.accepted().build();
    }


    @GetMapping("/streams/metrics")
    public ResponseEntity<Map<String, Object>> streamMetrics() {
        return ResponseEntity.ok(Map.of("metrics", streamMetricService.latestMetrics()));
    }
}
