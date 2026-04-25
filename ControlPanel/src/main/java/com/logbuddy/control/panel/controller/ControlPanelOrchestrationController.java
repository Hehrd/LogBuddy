package com.logbuddy.control.panel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/control-plane")
public class ControlPanelOrchestrationController extends ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(ControlPanelOrchestrationController.class);
    private static final String SPARK_HOST = "http://localhost:16000/control-plane";
    private static final String DATA_HOST = "http://localhost:6969/api/control-plane";

    @Autowired
    public ControlPanelOrchestrationController(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Aggregating health from {} and {}", SPARK_HOST, DATA_HOST);
        return ResponseEntity.ok(Map.of(
                "spark", restTemplate.getForEntity(SPARK_HOST + "/health", Void.class).getStatusCode().is2xxSuccessful(),
                "data", restTemplate.getForEntity(DATA_HOST + "/health", Void.class).getStatusCode().is2xxSuccessful()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.debug("Aggregating status from {} and {}", SPARK_HOST, DATA_HOST);
        Map<?, ?> spark = restTemplate.getForObject(SPARK_HOST + "/status", Map.class);
        Map<?, ?> data = restTemplate.getForObject(DATA_HOST + "/status", Map.class);
        return ResponseEntity.ok(Map.of("spark", spark, "data", data));
    }

    @PostMapping("/sleep")
    public ResponseEntity<Void> sleep() {
        log.info("Forwarding sleep request to SparkProcessing and DataProcessing");
        restTemplate.postForEntity(SPARK_HOST + "/sleep", null, Void.class);
        restTemplate.postForEntity(DATA_HOST + "/sleep", null, Void.class);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/wake")
    public ResponseEntity<Void> wake() {
        log.info("Forwarding wake request to SparkProcessing and DataProcessing");
        restTemplate.postForEntity(SPARK_HOST + "/wake", null, Void.class);
        restTemplate.postForEntity(DATA_HOST + "/wake", null, Void.class);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restart")
    public ResponseEntity<Void> restart() {
        log.info("Forwarding restart request to SparkProcessing and DataProcessing");
        restTemplate.postForEntity(SPARK_HOST + "/restart", null, Void.class);
        restTemplate.postForEntity(DATA_HOST + "/restart", null, Void.class);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Void> shutdown() {
        log.warn("Forwarding shutdown request to SparkProcessing and DataProcessing");
        restTemplate.postForEntity(SPARK_HOST + "/shutdown", null, Void.class);
        restTemplate.postForEntity(DATA_HOST + "/shutdown", null, Void.class);
        return ResponseEntity.accepted().build();
    }
}
