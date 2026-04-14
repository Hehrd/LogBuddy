package com.logbuddy.control.panel.controller;

import com.sun.net.httpserver.HttpExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/spark")
public class SparkControlPanelController extends ControlPanelController {
    private final String SPARK_HOST = "localhost";

    @Autowired
    public SparkControlPanelController(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @GetMapping("/reload-settings")
    private ResponseEntity<Void> handleReloadSettings() {
        return restTemplate.getForEntity(SPARK_HOST + "/reload-settings", Void.class);
    }

    @GetMapping("/stop-query/{queryId}")
    private ResponseEntity<Void> handleStopQuery(@PathVariable(name = "queryId") String queryId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Query-Id", queryId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                SPARK_HOST + "/stop-query",
                HttpMethod.GET,
                entity,
                Void.class);
    }

    @GetMapping("/list-queries")
    private ResponseEntity<List<String>> handleListQueries() {
        return restTemplate.exchange(
                SPARK_HOST + "/list-queries",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {});
    }
}
