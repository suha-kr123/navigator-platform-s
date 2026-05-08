package com.nivasafinance.features.call.service;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CallNotificationSseService {

    // Maximum connections allowed per user - reduced to prevent HTTP/2 stream exhaustion
    // 3 is reasonable (main tab, mobile, backup) - too many connections create too many HTTP/2 streams
    private static final int MAX_CONNECTIONS_PER_USER = 3;
    
    // Heartbeat interval in seconds - increased to reduce traffic and HTTP/2 stream pressure
    // 30 seconds is still frequent enough to keep connections alive through proxies/LBs
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

    // Store active SSE connections by username - supports multiple connections per user
    private final Map<String, List<SseEmitter>> activeConnections = new ConcurrentHashMap<>();
    
    // Store heartbeat tasks for each connection to allow cancellation
    private final Map<SseEmitter, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();
    
    // Executor service for scheduling heartbeats - increased thread pool to handle more concurrent heartbeats
    // Use larger pool based on expected connections (e.g., 50 connections = ~2 heartbeats/sec)
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(10);
    
    // Locks per emitter to prevent concurrent writes (HTTP/2 requirement)
    // HTTP/2 requires serialized writes per stream to avoid protocol errors
    private final Map<SseEmitter, Object> emitterLocks = new ConcurrentHashMap<>();

    /**
     * Register a new SSE connection for a user
     * Supports multiple connections per user (e.g., UAT + local both connected)
     * 
     * @param username The username for the connection
     * @return SseEmitter for the connection
     */
    public SseEmitter createConnection(String username) {
        // Get or create list of connections for this user
        List<SseEmitter> connections = activeConnections.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>());

        // Check connection limit - close oldest connection if limit reached
        if (connections.size() >= MAX_CONNECTIONS_PER_USER) {
            log.warn("⚠️ User {} has reached max connections ({}). Closing oldest connection.", 
                    username, MAX_CONNECTIONS_PER_USER);
            SseEmitter oldest = connections.remove(0);
            cancelHeartbeat(oldest);
            emitterLocks.remove(oldest);
            try {
                oldest.complete();
                log.info("Closed oldest connection for user: {}", username);
            } catch (Exception e) {
                log.debug("Error completing oldest connection for user: {}", username);
            }
        }

        // Create new SSE emitter with infinite timeout (0L)
        // Heartbeats will keep the connection alive through proxies/LBs
        SseEmitter emitter = new SseEmitter(0L);
        
        // Create lock for this emitter to prevent concurrent writes (HTTP/2 requirement)
        emitterLocks.put(emitter, new Object());
        
        // Handle completion - remove from list and cancel heartbeat
        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for user: {}", username);
            cancelHeartbeat(emitter);
            removeConnection(username, emitter);
        });
        
        // Handle timeout - should not happen with infinite timeout, but handle just in case
        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for user: {} (unexpected with infinite timeout)", username);
            cancelHeartbeat(emitter);
            removeConnection(username, emitter);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing SSE connection on timeout for user: {}", username);
            }
        });
        
        // Handle error - remove from list and cancel heartbeat
        emitter.onError((ex) -> {
            // Don't log as ERROR for expected disconnections (IOException)
            if (ex instanceof IOException) {
                log.info("SSE connection closed for user: {} - {}", username, ex.getMessage());
            } else {
                log.error("Unexpected SSE connection error for user: {}", username, ex);
            }
            cancelHeartbeat(emitter);
            removeConnection(username, emitter);
            try {
                emitter.completeWithError(ex);
            } catch (Exception e) {
                log.debug("Error completing SSE connection on error for user: {}", username);
            }
        });

        // Add to connections list
        connections.add(emitter);
        log.info("Added SSE connection for user: {}. Total connections for user: {}", 
                username, connections.size());
        
        // Send initial connection event with synchronization to prevent concurrent writes
        Object lock = emitterLocks.get(emitter);
        synchronized (lock) {
            try {
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("Connection established"));
            } catch (IOException e) {
                log.error("Failed to send initial SSE event to user: {}", username, e);
                cancelHeartbeat(emitter);
                removeConnection(username, emitter);
                emitter.completeWithError(e);
                return emitter;
            }
        }
        
        // Start heartbeat to keep connection alive through proxies/LBs
        startHeartbeat(username, emitter);
        
        return emitter;
    }

    /**
     * Remove a connection from the list for a user
     */
    private void removeConnection(String username, SseEmitter emitter) {
        List<SseEmitter> connections = activeConnections.get(username);
        if (connections != null) {
            connections.remove(emitter);
            // Remove lock when connection is removed
            emitterLocks.remove(emitter);
            // Remove user entry if no connections left
            if (connections.isEmpty()) {
                activeConnections.remove(username);
            }
        }
    }

    /**
     * Start heartbeat for a connection to keep it alive through proxies/LBs
     */
    private void startHeartbeat(String username, SseEmitter emitter) {
        ScheduledFuture<?> heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(() -> {
            List<SseEmitter> connections = activeConnections.get(username);
            // Check if connection is still active
            if (connections == null || !connections.contains(emitter)) {
                // Connection was removed, cancel this heartbeat
                cancelHeartbeat(emitter);
                return;
            }
            
            // Get lock for this emitter
            Object lock = emitterLocks.get(emitter);
            if (lock == null) {
                // Emitter was removed, cancel heartbeat
                cancelHeartbeat(emitter);
                return;
            }
            
            // Synchronize to prevent concurrent writes (HTTP/2 requirement)
            synchronized (lock) {
                // Double-check connection is still active after acquiring lock
                connections = activeConnections.get(username);
                if (connections == null || !connections.contains(emitter)) {
                    cancelHeartbeat(emitter);
                    return;
                }
                
                try {
                    // Use named event so frontend can track heartbeat timing for diagnostics
                    emitter.send(SseEmitter.event().name("heartbeat").data("keep-alive"));
                    log.debug("💓 Sent heartbeat to user: {}", username);
                } catch (IOException e) {
                    // Only remove connection on IOException (actual connection closed)
                    log.info("⚠️ Failed to send heartbeat to user: {} (connection closed): {}", username, e.getMessage());
                    cancelHeartbeat(emitter);
                    removeConnection(username, emitter);
                } catch (Exception e) {
                    // Log but don't remove connection for other exceptions - might be transient
                    // Let it retry on next heartbeat interval
                    log.warn("⚠️ Error sending heartbeat to user: {} (will retry next interval): {}", 
                            username, e.getMessage());
                    // Don't remove connection - allow retry on next heartbeat
                }
            }
        }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        
        heartbeatTasks.put(emitter, heartbeatTask);
        log.info("✅ Started heartbeat for user: {} (interval: {}s)", username, HEARTBEAT_INTERVAL_SECONDS);
    }

    /**
     * Cancel heartbeat task for a connection
     */
    private void cancelHeartbeat(SseEmitter emitter) {
        ScheduledFuture<?> task = heartbeatTasks.remove(emitter);
        if (task != null) {
            task.cancel(false);
            log.debug("Cancelled heartbeat for connection");
        }
    }

    /**
     * Send notification to a specific user via SSE
     * Sends to ALL active connections for the user (supports multiple tabs/devices)
     */
    public void sendNotificationToUser(CallNotificationResponse notification, String username) {
        if (username == null || username.isBlank()) {
            log.warn("⚠️ Cannot send notification: username is null or blank");
            return;
        }

        List<SseEmitter> connections = activeConnections.get(username);
        if (connections == null || connections.isEmpty()) {
            log.warn("⚠️ No active SSE connection found for username: {} (callSid: {})",
                    username, notification.getCallSid());
            log.warn("📋 Available connections are: {}", activeConnections.keySet());
            return;
        }

        log.info("📤 Sending notification to {} connection(s) for username: {}, callSid: {}",
                connections.size(), username, notification.getCallSid());

        // Send to ALL connections for this user
        List<SseEmitter> deadConnections = new ArrayList<>();
        int successCount = 0;

        for (SseEmitter emitter : connections) {
            Object lock = emitterLocks.get(emitter);
            if (lock == null) {
                // Emitter was removed, mark as dead
                deadConnections.add(emitter);
                continue;
            }
            
            // Synchronize to prevent concurrent writes with heartbeat
            synchronized (lock) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("call-notification")
                            .data(notification));
                    successCount++;
                    log.debug("✅ Sent notification to connection for user: {}", username);
                } catch (IOException e) {
                    log.warn("⚠️ Failed to send to one connection for user: {} (connection may be closed), callSid: {}",
                            username, notification.getCallSid());
                    deadConnections.add(emitter);
                }
            }
        }

        // Remove dead connections
        if (!deadConnections.isEmpty()) {
            connections.removeAll(deadConnections);
            for (SseEmitter dead : deadConnections) {
                cancelHeartbeat(dead);
                emitterLocks.remove(dead);
                try {
                    dead.completeWithError(new IOException("Connection closed"));
                } catch (Exception e) {
                    log.debug("Error completing dead connection for user: {}", username);
                }
            }

            // Remove user entry if no connections left
            if (connections.isEmpty()) {
                activeConnections.remove(username);
            }
        }

        if (successCount > 0) {
            log.info("✅ Successfully sent SSE notification to {} connection(s) for username: {}, callSid: {}",
                    successCount, username, notification.getCallSid());
        } else {
            log.warn("⚠️ Failed to send notification to any connection for username: {}, callSid: {}",
                    username, notification.getCallSid());
        }
    }

    /**
     * Close all connections for a user
     */
    public void closeConnection(String username) {
        List<SseEmitter> connections = activeConnections.remove(username);
        if (connections != null && !connections.isEmpty()) {
            log.info("Closing {} connection(s) for user: {}", connections.size(), username);
            for (SseEmitter emitter : connections) {
                cancelHeartbeat(emitter);
                emitterLocks.remove(emitter);
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.debug("Error completing SSE connection for user: {}", username);
                }
            }
        }
    }

    /**
     * Get count of unique users with active connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * Get total count of all active connections (across all users)
     */
    public int getTotalConnectionCount() {
        return activeConnections.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Subscribe to call notifications for the current user (from UserContext)
     * 
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

        log.info("🔌 SSE connection request from user: {}", username);
        SseEmitter emitter = createConnection(username);
        List<SseEmitter> userConnections = activeConnections.get(username);
        int userConnectionCount = userConnections != null ? userConnections.size() : 0;
        log.info("✅ SSE connection created for user: {}. User has {} connection(s). Total users: {}, Total connections: {}",
                username, userConnectionCount, activeConnections.size(), getTotalConnectionCount());
        log.info("📋 All active users: {}", activeConnections.keySet());
        return emitter;
    }

    /**
     * Unsubscribe from call notifications for the current user (from UserContext)
     * 
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

    /**
     * Cleanup method to shut down executor service when bean is destroyed
     */
    @PreDestroy
    public void destroy() {
        log.info("Shutting down heartbeat executor service");
        heartbeatExecutor.shutdown();
        try {
            if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Heartbeat executor did not terminate gracefully, forcing shutdown");
                heartbeatExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for heartbeat executor to shutdown", e);
            heartbeatExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
