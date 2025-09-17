package com.paklog.warehouse.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple message broker for topics
        config.enableSimpleBroker("/topic");
        
        // Set application destination prefix for messages from client
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for individual user messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint for WebSocket connections
        registry.addEndpoint("/ws/mobile")
                .setAllowedOriginPatterns("*") // Configure based on your needs
                .withSockJS(); // Enable SockJS fallback
        
        // Register endpoint for direct WebSocket connections (no SockJS)
        registry.addEndpoint("/ws/mobile-direct")
                .setAllowedOriginPatterns("*");
    }
}