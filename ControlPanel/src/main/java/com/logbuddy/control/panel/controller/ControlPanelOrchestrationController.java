package com.logbuddy.control.panel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/control-panel")
public class ControlPanelOrchestrationController extends ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(ControlPanelOrchestrationController.class);
    private static final String SPARK_HOST = "http://localhost:16000/control-panel";
    private static final String DATA_HOST = "http://localhost:6969/control-panel";

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
}
