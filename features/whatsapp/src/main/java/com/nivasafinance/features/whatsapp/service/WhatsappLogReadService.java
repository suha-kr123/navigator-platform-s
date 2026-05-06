package com.nivasafinance.features.whatsapp.service;

import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogLead;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WhatsappLogReadService {

    List<WhatsappLogResponse> getWhatsappLogsByIds(List<Long> ids);

    Page<WhatsappLogLead> findLeadMappingsByLeadId(Long leadId, WhatsappCreatedSource createdSource, Pageable pageable);

    Page<WhatsappLogAdvisor> findAdvisorMappingsByAdvisorId(Long advisorId, WhatsappCreatedSource createdSource, Pageable pageable);
}
