package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadWhatsappNotificationResponse;

import java.util.UUID;

public interface LeadWhatsappReadService {

    PaginatedResponse<LeadWhatsappNotificationResponse> getWhatsappNotifications(
            UUID leadIdentifier,
            PaginationRequest paginationRequest
    );
}
