package com.alexander.processing.service.alert;

import com.alexander.processing.config.AlertWebSocketConfig;
import com.alexander.processing.exception.runtime.AlertPublishingException;
import com.alexander.processing.model.alert.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Service
public class AlertingService {
    private static final Logger log = LoggerFactory.getLogger(AlertingService.class);
    private final WebClient webClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final String aiAnalyzerHost;

    public AlertingService(WebClient webClient,
                           SimpMessagingTemplate messagingTemplate,
                           @Value("${ai-analyzer.host}") String aiAnalyzerHost) {
        this.webClient = webClient;
        this.messagingTemplate = messagingTemplate;
        this.aiAnalyzerHost = aiAnalyzerHost;
    }

    public void sendAlert(List<String> endpoints, Alert alert) {
    if (alert.aiOverviewEnabled()) {
        redirectToAIAnalyzer(alert, endpoints);
        return;
    }
        for (String endpoint : endpoints) {
            webClient.post()
                    .uri(endpoint)
                    .bodyValue(alert)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnError(e -> log.error("Failed to send alert to {}: {}", endpoint, e.getMessage()))
                    .subscribe();
        }
        pushAlertToDashboard(alert);
    }

    private void redirectToAIAnalyzer(Alert alert, List<String> endpoints) {
        webClient.post()
                .uri(aiAnalyzerHost + "/analyze")
                .bodyValue(new AiAnalyzerRequest(alert, endpoints))
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> log.error("Failed to send alert to AI analyzer: {}", e.getMessage()))
                .subscribe();

    }

    private void pushAlertToDashboard(Alert alert) {
        try {
            messagingTemplate.convertAndSend(AlertWebSocketConfig.ALERTS_TOPIC, alert);
        } catch (MessagingException e) {
            AlertPublishingException exception =
                    new AlertPublishingException("Failed to publish alert to websocket subscribers", e);
            log.error(exception.getMessage(), exception);
        }
    }

    private class AiAnalyzerRequest {
        private final Alert alert;
        private final List<String> endpoints;

        public AiAnalyzerRequest(Alert alert, List<String> endpoints) {
            this.alert = alert;
            this.endpoints = endpoints;
        }

        public Alert getAlert() {
            return alert;
        }

        public List<String> getEndpoints() {
            return endpoints;
        }
    }
}
