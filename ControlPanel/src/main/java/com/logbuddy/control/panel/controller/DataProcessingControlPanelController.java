package com.logbuddy.control.panel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/control-panel/data-processing")
public class DataProcessingControlPanelController extends ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(DataProcessingControlPanelController.class);
    private static final String DP_HOST = "http://localhost:6969/api/control-panel";

    @Autowired
    public DataProcessingControlPanelController(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        log.debug("Proxying DataProcessing health request to {}", DP_HOST);
        return restTemplate.getForEntity(DP_HOST + "/health", Void.class);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.debug("Proxying DataProcessing status request to {}", DP_HOST);
        return exchangeMap(DP_HOST + "/status", HttpMethod.GET);
    }

    private ResponseEntity<Map<String, Object>> exchangeMap(String url, HttpMethod method) {
        return restTemplate.exchange(url, method, null, new ParameterizedTypeReference<>() {});
    }
}
