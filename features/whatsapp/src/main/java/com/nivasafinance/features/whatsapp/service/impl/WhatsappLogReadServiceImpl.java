package com.nivasafinance.features.whatsapp.service.impl;

import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import com.nivasafinance.features.whatsapp.entity.WhatsappLog;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogLead;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import com.nivasafinance.features.whatsapp.enums.WhatsappStatus;
import com.nivasafinance.features.whatsapp.repository.WhatsappLogAdvisorRepository;
import com.nivasafinance.features.whatsapp.repository.WhatsappLogLeadRepository;
import com.nivasafinance.features.whatsapp.repository.WhatsappLogRepository;
import com.nivasafinance.features.whatsapp.service.WhatsappLogReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class
WhatsappLogReadServiceImpl implements WhatsappLogReadService {

    private static final String SENDER_CUSTOMER = "Customer";
    private static final String SENDER_NIVASA = "Nivasa Finance";

    private final WhatsappLogRepository whatsappLogRepository;
    private final WhatsappLogLeadRepository whatsappLogLeadRepository;
    private final WhatsappLogAdvisorRepository whatsappLogAdvisorRepository;

    @Override
    public List<WhatsappLogResponse> getWhatsappLogsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        try {
            Map<Long, WhatsappLogResponse> byId = whatsappLogRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(WhatsappLog::getId, this::toResponse));
            List<WhatsappLogResponse> ordered = ids.stream()
                    .map(byId::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (ordered.size() != ids.size()) {
                log.warn("Whatsapp log lookup mismatch: requested {} ids, found {}", ids.size(), ordered.size());
            }
            return ordered;
        } catch (DataAccessException e) {
            log.error("DB error fetching whatsapp logs by ids (size={}): {}", ids.size(), e.getMessage(), e);
            throw new RuntimeException("Failed to fetch whatsapp logs by ids", e);
        }
    }

    @Override
    public Page<WhatsappLogLead> findLeadMappingsByLeadId(Long leadId, WhatsappCreatedSource createdSource, Pageable pageable) {
        log.debug("Fetching whatsapp_log_lead mappings: leadId={}, createdSource={}", leadId, createdSource);
        try {
            if (createdSource == null) {
                return whatsappLogLeadRepository.findByLeadIdOrderByWhatsappLogIdDesc(leadId, pageable);
            }
            return whatsappLogLeadRepository.findByLeadIdAndCreatedSource(leadId, createdSource, pageable);
        } catch (DataAccessException e) {
            log.error("DB error fetching mappings for leadId={}, createdSource={}: {}",
                    leadId, createdSource, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch whatsapp log lead mappings", e);
        }
    }

    @Override
    public Page<WhatsappLogAdvisor> findAdvisorMappingsByAdvisorId(Long advisorId, WhatsappCreatedSource createdSource, Pageable pageable) {
        log.debug("Fetching whatsapp_log_advisor mappings: advisorId={}, createdSource={}", advisorId, createdSource);
        try {
            if (createdSource == null) {
                return whatsappLogAdvisorRepository.findByAdvisorIdOrderByWhatsappLogIdDesc(advisorId, pageable);
            }
            return whatsappLogAdvisorRepository.findByAdvisorIdAndCreatedSource(advisorId, createdSource, pageable);
        } catch (DataAccessException e) {
            log.error("DB error fetching mappings for advisorId={}, createdSource={}: {}",
                    advisorId, createdSource, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch whatsapp log advisor mappings", e);
        }
    }

    private WhatsappLogResponse toResponse(WhatsappLog log) {
        WhatsappStatus dirStatus = log.getStatus();
        return WhatsappLogResponse.builder()
                .id(log.getId())
                .identifier(log.getIdentifier())
                .direction(dirStatus != null ? dirStatus.name() : null)
                .senderLabel(senderLabel(dirStatus))
                .messageType(log.getMessageType() != null ? log.getMessageType().name().toLowerCase() : null)
                .messageData(log.getMessageData())
                .templateDetails(log.getTemplateDetails())
                .status(deliveryStatus(dirStatus))
                .createdSourceType(log.getCreatedSource() != null ? log.getCreatedSource().name() : null)
                .messageTime(log.getMessageTime())
                .build();
    }

    private String senderLabel(WhatsappStatus status) {
        if (status == WhatsappStatus.RECEIVED) {
            return SENDER_CUSTOMER;
        }
        if (status == WhatsappStatus.SENT) {
            return SENDER_NIVASA;
        }
        return null;
    }

    private String deliveryStatus(WhatsappStatus status) {
        if (status == WhatsappStatus.RECEIVED) {
            return "DELIVERED";
        }
        if (status == WhatsappStatus.SENT) {
            return "SENT";
        }
        return null;
    }
}
