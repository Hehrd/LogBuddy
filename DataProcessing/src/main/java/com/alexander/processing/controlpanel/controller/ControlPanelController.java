package com.alexander.processing.controlpanel.controller;

import com.alexander.processing.service.ds.DataSourceIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-panel")
public class ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(ControlPanelController.class);

    private final DataSourceIngestService dataSourceIngestService;

    public ControlPanelController(DataSourceIngestService dataSourceIngestService) {
        this.dataSourceIngestService = dataSourceIngestService;
    }

    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        log.debug("Received health check request");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/status")
    public ResponseEntity<Void> status() {
        log.debug("Received status check request");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/sleep")
    public ResponseEntity<Void> sleep() {
        log.info("Pausing data source ingest via control panel");
        dataSourceIngestService.sleep();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/wake")
    public ResponseEntity<Void> wake() {
        log.info("Resuming data source ingest via control panel");
        dataSourceIngestService.wake();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/restart")
    public ResponseEntity<Void> restart() {
        log.info("Restart requested via control panel but not implemented");
        dataSourceIngestService.sleep();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
