package com.logbuddy.control.panel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/control-plane/data-processing")
public class DataProcessingControlPanelController extends ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(DataProcessingControlPanelController.class);
    private static final String DP_HOST = "http://localhost:6969/api/control-plane";

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

    @PostMapping("/sleep")
    public ResponseEntity<Void> sleep() {
        log.info("Proxying DataProcessing sleep request");
        return restTemplate.postForEntity(DP_HOST + "/sleep", null, Void.class);
    }

    @PostMapping("/wake")
    public ResponseEntity<Void> wake() {
        log.info("Proxying DataProcessing wake request");
        return restTemplate.postForEntity(DP_HOST + "/wake", null, Void.class);
    }

    @PostMapping("/restart")
    public ResponseEntity<Void> restart() {
        log.info("Proxying DataProcessing restart request");
        return restTemplate.postForEntity(DP_HOST + "/restart", null, Void.class);
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Void> shutdown() {
        log.warn("Proxying DataProcessing shutdown request");
        return restTemplate.postForEntity(DP_HOST + "/shutdown", null, Void.class);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        log.debug("Proxying DataProcessing config request");
        return exchangeMap(DP_HOST + "/config", HttpMethod.GET);
    }

    @PostMapping("/config/reload")
    public ResponseEntity<Map<String, Object>> reloadConfig() {
        log.info("Proxying DataProcessing config reload request");
        return exchangeMap(DP_HOST + "/config/reload", HttpMethod.POST);
    }

    @PostMapping("/config/validate")
    public ResponseEntity<Map<String, Object>> validateConfig() {
        log.info("Proxying DataProcessing config validation request");
        return exchangeMap(DP_HOST + "/config/validate", HttpMethod.POST);
    }

    @GetMapping("/datasources")
    public ResponseEntity<Map<String, Object>> dataSources() {
        log.debug("Proxying DataProcessing datasources request");
        return exchangeMap(DP_HOST + "/datasources", HttpMethod.GET);
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> rules() {
        log.debug("Proxying DataProcessing rules request");
        return exchangeMap(DP_HOST + "/rules", HttpMethod.GET);
    }

    private ResponseEntity<Map<String, Object>> exchangeMap(String url, HttpMethod method) {
        return restTemplate.exchange(url, method, null, new ParameterizedTypeReference<>() {});
    }

}
