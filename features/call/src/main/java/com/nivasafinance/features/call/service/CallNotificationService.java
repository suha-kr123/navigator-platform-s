package com.nivasafinance.features.call.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.EnrichedCallNotificationResponse;

public interface CallNotificationService {
    PaginatedResponse<EnrichedCallNotificationResponse> getNotificationsForCurrentUser(PaginationRequest paginationRequest);
}

