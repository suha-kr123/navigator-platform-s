package com.nivasafinance.common.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket handshake handler that extracts user identity from X-Username header.
 * Sets Principal to username/email (what API Gateway sends) for WebSocket user destination routing.
 * 
 * Security model:
 * - API Gateway validates JWT and forwards X-Username header (contains username/email)
 * - Backend trusts X-Username header (no JWT re-validation needed)
 * - Principal.getName() returns username/email for /user destination routing
 * - This matches what convertAndSendToUser() expects
 */
@Slf4j
public class WebSocketHandshakeHandler extends DefaultHandshakeHandler {

    private static final String USERNAME_HEADER = "X-Username";

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String username = servletRequest.getServletRequest().getHeader(USERNAME_HEADER);

            if (username == null || username.isBlank()) {
                log.warn("WebSocket handshake rejected: Missing X-Username header from {}", 
                        request.getRemoteAddress());
                return null;
            }

            log.debug("WebSocket handshake authorized for user: {}", username);
            return () -> username;
        }
        return null;
    }
}


