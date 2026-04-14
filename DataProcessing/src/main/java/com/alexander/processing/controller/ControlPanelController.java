package com.alexander.processing.controller;

import com.alexander.processing.data.service.ds.DataSourceIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/control-panel")
public class ControlPanelController {
    private final DataSourceIngestService dataSourceIngestService;

    public ControlPanelController(DataSourceIngestService dataSourceIngestService) {
        this.dataSourceIngestService = dataSourceIngestService;
    }

    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/status")
    public ResponseEntity<Void> status() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/sleep")
    public ResponseEntity<Void> sleep() {
        dataSourceIngestService.sleep();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/wake")
    public ResponseEntity<Void> wake() {
        dataSourceIngestService.wake();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/restart")
    public ResponseEntity<Void> restart() {
        dataSourceIngestService.sleep();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


}

