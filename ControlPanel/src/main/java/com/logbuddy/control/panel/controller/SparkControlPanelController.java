package com.logbuddy.control.panel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/control-panel")
public class SparkControlPanelController extends ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(SparkControlPanelController.class);
    private static final String SPARK_HOST = "http://localhost:16000/control-panel";

    @Autowired
    public SparkControlPanelController(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @GetMapping("/queries")
    public ResponseEntity<Map<String, Object>> queries() {
        log.debug("Proxying SparkProcessing queries request");
        return exchangeMap(SPARK_HOST + "/queries", HttpMethod.GET, null);
    }

    @PostMapping("/queries/{dataSource}/start")
    public ResponseEntity<Void> startQuery(@PathVariable String dataSource) {
        log.info("Proxying SparkProcessing start query request for {}", dataSource);
        return restTemplate.postForEntity(SPARK_HOST + "/queries/" + dataSource + "/start", null, Void.class);
    }

    @PostMapping("/queries/{dataSource}/stop")
    public ResponseEntity<Void> stopQuery(@PathVariable String dataSource) {
        log.info("Proxying SparkProcessing stop query request for {}", dataSource);
        return restTemplate.postForEntity(SPARK_HOST + "/queries/" + dataSource + "/stop", null, Void.class);
    }

    @PostMapping("/queries/restart")
    public ResponseEntity<Void> restartQueries() {
        log.info("Proxying SparkProcessing restart queries request");
        return restTemplate.postForEntity(SPARK_HOST + "/queries/restart", null, Void.class);
    }

    @GetMapping("/queries/{dataSource}")
    public ResponseEntity<Map<String, Object>> queryStatus(@PathVariable String dataSource) {
        log.debug("Proxying SparkProcessing query status request for {}", dataSource);
        return exchangeMap(SPARK_HOST + "/queries/" + dataSource, HttpMethod.GET, null);
    }

    @GetMapping("/streams/metrics")
    public ResponseEntity<Map<String, Object>> streamMetrics() {
        log.debug("Proxying SparkProcessing stream metrics request");
        return exchangeMap(SPARK_HOST + "/streams/metrics", HttpMethod.GET, null);
    }

    private ResponseEntity<Map<String, Object>> exchangeMap(String url, HttpMethod method, HttpEntity<?> entity) {
        return restTemplate.exchange(url, method, entity, new ParameterizedTypeReference<>() {});
    }
}
