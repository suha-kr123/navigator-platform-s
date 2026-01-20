package com.nivasafinance.features.call.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.common.dto.EnrichedCallNotificationResponse;

import java.util.Optional;

public interface CallNotificationService {
    PaginatedResponse<EnrichedCallNotificationResponse> getNotificationsForCurrentUser(PaginationRequest paginationRequest);
    
    /**
     * Get the most recent call notification from Redis for the current user (for reconnection scenarios).
     * See implementation for detailed documentation.
     * 
     * @return Optional containing the most recent notification from Redis, or empty if none found
     */
    Optional<CallNotificationResponse> getRecentNotificationsFromRedis();
}

