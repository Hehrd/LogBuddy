package com.alexander.processing.service.alert;

import com.alexander.processing.config.AlertWebSocketConfig;
import com.alexander.processing.exception.runtime.AlertPublishingException;
import com.alexander.processing.model.alert.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
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
        String uri = buildAiAnalyzerUri();
        webClient.post()
                .uri(uri)
                .bodyValue(new AiAnalyzerRequest(alert, endpoints))
                .retrieve()
                .bodyToMono(AiAnalyzerResponse.class)
                .doOnNext(response -> pushAlertToDashboard(response.alert()))
                .doOnError(WebClientResponseException.class, e ->
                        log.error("AI analyzer request failed with status {} and body {}",
                                e.getStatusCode(), e.getResponseBodyAsString()))
                .doOnError(e -> log.error("Failed to send alert to AI analyzer: {}", e.getMessage(), e))
                .block();
    }

    private String buildAiAnalyzerUri() {
        String baseHost = aiAnalyzerHost == null ? "" : aiAnalyzerHost.trim();
        if (!baseHost.startsWith("http://") && !baseHost.startsWith("https://")) {
            baseHost = "http://" + baseHost;
        }

        return URI.create(baseHost)
                .resolve("/api/v1/log-analysis")
                .toString();
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

    private record AiAnalyzerRequest(Alert alert, List<String> endpoints) {
    }

    private record AiAnalyzerResponse(Alert alert) {
    }
}
