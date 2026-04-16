package com.alexander.processing.data.service.alert;

import com.alexander.processing.Main;
import com.alexander.processing.data.config.AlertWebSocketConfig;
import com.alexander.processing.data.model.alert.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Service
public class    AlertingService {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private final WebClient webClient;
    private final SimpMessagingTemplate messagingTemplate;


    public AlertingService(WebClient webClient, SimpMessagingTemplate messagingTemplate) {
        this.webClient = webClient;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendAlert(List<String> endpoints, Alert alert) {
        pushAlertToDashboard(alert);

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

    private void pushAlertToDashboard(Alert alert) {
        try {
            messagingTemplate.convertAndSend(AlertWebSocketConfig.ALERTS_TOPIC, alert);
        } catch (Exception e) {
            log.error("Failed to publish alert to websocket subscribers: {}", e.getMessage());
        }
    }
}
