package com.alexander.processing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class AlertWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String ALERTS_TOPIC = "/topic/alerts";
    public static final String ALERTS_ENDPOINT = "/ws/alerts";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ALERTS_ENDPOINT)
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://localhost:5173"
                );
    }
}