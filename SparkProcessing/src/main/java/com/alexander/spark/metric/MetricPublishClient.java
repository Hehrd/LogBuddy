package com.alexander.spark.metric;

import com.alexander.spark.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MetricPublishClient {
    private static final Logger log = LogManager.getLogger(MetricPublishClient.class);

    private static volatile HttpClient CLIENT;
    private static volatile URI METRICS_URI;

    private MetricPublishClient() {
    }

    public static void publish(String host, Integer port, StreamMetricsUpdate update) {
        if (host == null || port == null || update.metrics().isEmpty()) {
            return;
        }
        URI targetUri = getMetricsUri(host, port);
        HttpClient client = getClient();
        try {
            String body = JsonUtil.serialize(update);
            HttpRequest request = HttpRequest.newBuilder(targetUri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 400) {
                log.warn("Failed to publish stream metrics to {}. HTTP status {}", targetUri, response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Failed to publish stream metrics to {}", targetUri, e);
        }
    }

    private static HttpClient getClient() {
        if (CLIENT == null) {
            synchronized (MetricPublishClient.class) {
                if (CLIENT == null) {
                    CLIENT = HttpClient.newHttpClient();
                }
            }
        }
        return CLIENT;
    }

    private static URI getMetricsUri(String host, int port) {
        URI current = METRICS_URI;
        String expected = "http://" + host + ":" + port + "/api/metrics/stream";
        if (current == null || !expected.equals(current.toString())) {
            synchronized (MetricPublishClient.class) {
                current = METRICS_URI;
                if (current == null || !expected.equals(current.toString())) {
                    METRICS_URI = URI.create(expected);
                    current = METRICS_URI;
                    log.info("Creating metric publish client for {}", current);
                }
            }
        }
        return current;
    }
}
