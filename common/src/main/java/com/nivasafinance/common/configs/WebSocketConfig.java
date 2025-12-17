package com.nivasafinance.common.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Reusable WebSocket configuration for real-time messaging.
 * Configures STOMP over WebSocket with optional SockJS fallback and broker relay support.
 * 
 * Configuration properties:
 * - websocket.endpoint: WebSocket endpoint path (default: /ws)
 * - websocket.allowed-origins: Comma-separated list of allowed origins (default: *)
 * - websocket.enable-sockjs: Enable SockJS fallback (default: true, set to false in production)
 * - websocket.broker-relay-enabled: Use external message broker (RabbitMQ/ActiveMQ) (default: false)
 * - websocket.broker-relay-host: Broker host (default: localhost)
 * - websocket.broker-relay-port: Broker port (default: 61613)
 * - websocket.broker-relay-username: Broker username (optional)
 * - websocket.broker-relay-password: Broker password (optional)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.endpoint:/ws}")
    private String endpoint;

    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${websocket.enable-sockjs:true}")
    private boolean enableSockJS;

    @Value("${websocket.topic-prefix:/topic}")
    private String topicPrefix;

    @Value("${websocket.queue-prefix:/queue}")
    private String queuePrefix;

    @Value("${websocket.app-prefix:/app}")
    private String appPrefix;

    @Value("${websocket.broker-relay-enabled:false}")
    private boolean brokerRelayEnabled;

    @Value("${websocket.broker-relay-host:localhost}")
    private String brokerRelayHost;

    @Value("${websocket.broker-relay-port:61613}")
    private int brokerRelayPort;

    @Value("${websocket.broker-relay-username:}")
    private String brokerRelayUsername;

    @Value("${websocket.broker-relay-password:}")
    private String brokerRelayPassword;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if (brokerRelayEnabled) {
            config.enableStompBrokerRelay(topicPrefix, queuePrefix)
                    .setRelayHost(brokerRelayHost)
                    .setRelayPort(brokerRelayPort)
                    .setClientLogin(brokerRelayUsername)
                    .setClientPasscode(brokerRelayPassword)
                    .setSystemLogin(brokerRelayUsername)
                    .setSystemPasscode(brokerRelayPassword);
        } else {
            config.enableSimpleBroker(topicPrefix, queuePrefix);
        }
        config.setApplicationDestinationPrefixes(appPrefix);
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }

        var endpointRegistration = registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(origins)
                .setHandshakeHandler(new WebSocketHandshakeHandler());

        if (enableSockJS) {
            endpointRegistration.withSockJS();
        }
    }
}

