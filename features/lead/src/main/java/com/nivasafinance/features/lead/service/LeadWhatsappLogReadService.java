package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadWhatsappLogResponse;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogFilters;

import java.util.UUID;

public interface LeadWhatsappLogReadService {

    PaginatedResponse<LeadWhatsappLogResponse> getWhatsappMessages(
            UUID leadIdentifier,
            WhatsappLogFilters filters,
            PaginationRequest paginationRequest
    );
}
