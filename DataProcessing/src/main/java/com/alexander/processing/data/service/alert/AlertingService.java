package com.alexander.processing.data.service.alert;

import com.alexander.processing.Main;
import com.alexander.processing.data.model.alert.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Service
public class AlertingService {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private final WebClient webClient;


    @Autowired
    public AlertingService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendAlert(List<String> endpoints, Alert alert) {
        for (String endpoint : endpoints) {
            webClient.post()
                    .uri(endpoint)
                    .bodyValue(alert)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnError(e -> log.error("Failed to send alert to {}: {}", endpoint, e.getMessage()))
                    .subscribe();
        }
    }
}