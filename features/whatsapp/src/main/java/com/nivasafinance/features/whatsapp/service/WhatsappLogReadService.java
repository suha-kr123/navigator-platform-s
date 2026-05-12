package com.nivasafinance.features.whatsapp.service;

import com.nivasafinance.features.whatsapp.dto.WhatsappLogFilters;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WhatsappLogReadService {

    List<WhatsappLogResponse> getWhatsappLogsByIds(List<Long> ids);

    Page<Long> findLeadWhatsappLogIds(Long leadId, WhatsappLogFilters filters, Pageable pageable);

    Page<Long> findAdvisorWhatsappLogIds(Long advisorId, WhatsappLogFilters filters, Pageable pageable);
}
