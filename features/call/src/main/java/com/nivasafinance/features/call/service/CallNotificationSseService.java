package com.nivasafinance.features.call.service;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CallNotificationSseService {

    // Store active SSE connections by username
    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();

    /**
     * Register a new SSE connection for a user
     */
    public SseEmitter createConnection(String username) {
        // Remove any existing connection for this user
        SseEmitter existing = activeConnections.remove(username);
        if (existing != null) {
            try {
                existing.complete();
            } catch (Exception e) {
                log.debug("Error completing existing SSE connection for user: {}", username);
            }
        }

        // Create new SSE emitter with 30 minute timeout
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        // Handle completion and timeout
        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for user: {}", username);
            activeConnections.remove(username);
        });
        
        emitter.onTimeout(() -> {
            log.debug("SSE connection timeout for user: {}", username);
            activeConnections.remove(username);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing SSE connection on timeout for user: {}", username);
            }
        });
        
        emitter.onError((ex) -> {
            log.error("SSE connection error for user: {}", username, ex);
            activeConnections.remove(username);
            try {
                emitter.completeWithError(ex);
            } catch (Exception e) {
                log.debug("Error completing SSE connection on error for user: {}", username);
            }
        });

        activeConnections.put(username, emitter);
        
        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connection established"));
        } catch (IOException e) {
            log.error("Failed to send initial SSE event to user: {}", username, e);
            activeConnections.remove(username);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * Send notification to a specific user via SSE
     */
    public void sendNotificationToUser(CallNotificationResponse notification, String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        SseEmitter emitter = activeConnections.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("call-notification")
                        .data(notification));
            } catch (IOException e) {
                log.error("Failed to send SSE notification to user: {}", username, e);
                // Remove from map first to prevent race condition
                SseEmitter removed = activeConnections.remove(username);
                if (removed != null && removed == emitter) {
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ex) {
                        log.debug("Error completing SSE connection after send failure for user: {}", username);
                    }
                }
            }
        } else {
            log.debug("No active SSE connection found for user: {}", username);
        }
    }

    /**
     * Close connection for a user
     */
    public void closeConnection(String username) {
        SseEmitter emitter = activeConnections.remove(username);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing SSE connection for user: {}", username);
            }
        }
    }

    /**
     * Get count of active connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * Subscribe to call notifications for the current user (from UserContext)
     * @return SseEmitter for the connection, or completes with error if user not authenticated
     */
    public SseEmitter subscribeToCallNotifications() {
        String username = UserContext.getUsername();
        
        if (username == null || username.isBlank()) {
            log.warn("SSE connection rejected: Missing username in UserContext");
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new UnauthorizedException("User not authenticated"));
            return emitter;
        }
        
        log.debug("SSE connection request from user: {}", username);
        return createConnection(username);
    }

    /**
     * Unsubscribe from call notifications for the current user (from UserContext)
     * @throws UnauthorizedException if user is not authenticated
     */
    public void unsubscribeFromCallNotifications() {
        String username = UserContext.getUsername();
        
        if (username == null || username.isBlank()) {
            log.warn("SSE unsubscribe rejected: Missing username in UserContext");
            throw new UnauthorizedException("User not authenticated");
        }
        
        closeConnection(username);
    }
}

