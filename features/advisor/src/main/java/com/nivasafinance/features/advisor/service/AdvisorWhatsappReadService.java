package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappNotificationResponse;

import java.util.UUID;

public interface AdvisorWhatsappReadService {

    PaginatedResponse<AdvisorWhatsappNotificationResponse> getWhatsappNotifications(
            UUID advisorIdentifier,
            PaginationRequest paginationRequest
    );
}
