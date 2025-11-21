package com.nivasafinance.services.whatsapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nivasafinance.services.whatsapp.dto.TemplateLogRequest;
import com.nivasafinance.services.whatsapp.entity.WhatsAppLog;
import com.nivasafinance.services.whatsapp.repository.WhatsAppLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WhatsAppLogService {
    
    private final WhatsAppLogRepository whatsAppLogRepository;
    
    public WhatsAppLogService(WhatsAppLogRepository whatsAppLogRepository) {
        this.whatsAppLogRepository = whatsAppLogRepository;
    }
    
    public WhatsAppLog saveTemplateLog(WhatsAppLog messageLog) {
        return whatsAppLogRepository.save(messageLog);
    }

    public WhatsAppLog updateTemplateStatus(String messageId, String status, String deliveredAt) {
        WhatsAppLog messageLog = whatsAppLogRepository.findByMessageId(messageId);
        if (messageLog != null) {
            messageLog.setStatus(status);
            if (deliveredAt != null) {
                messageLog.setDeliveredTime(LocalDateTime.parse(deliveredAt));
            }
            return whatsAppLogRepository.save(messageLog);
        }
        return null;
    }
    
    public WhatsAppLog getTemplateLogByMessageId(String messageId) {
        return whatsAppLogRepository.findByMessageId(messageId);
    }
    
    public List<WhatsAppLog> getTemplateLogsByPhoneNumber(String phoneNumber) {
        return whatsAppLogRepository.findByPhoneNumber(phoneNumber);
    }
    
    public List<WhatsAppLog> getTemplateLogsByStatus(String status) {
        return whatsAppLogRepository.findByStatus(status);
    }
    
    public WhatsAppLog createTemplateLog(TemplateLogRequest request) {
        WhatsAppLog messageLog = new WhatsAppLog();
        messageLog.setMessageId(request.getMessageId());
        messageLog.setPhoneNumber(request.getPhoneNumber());
        messageLog.setTemplateName(request.getTemplateName());
        messageLog.setBroadcastName(request.getBroadcastName());
        messageLog.setStatus(request.getStatus());
        messageLog.setSentTime(LocalDateTime.now());
        return saveTemplateLog(messageLog);
    }

    public WhatsAppLog updateTemplateStatusByPhoneNumber(String phoneNumber, String status, String deliveredAt) {
        List<WhatsAppLog> logs = whatsAppLogRepository.findByPhoneNumber(phoneNumber);
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        WhatsAppLog messageLog = logs.get(0);
        messageLog.setStatus(status);
        if (deliveredAt != null) {
            messageLog.setDeliveredTime(LocalDateTime.parse(deliveredAt));
        }
        return whatsAppLogRepository.save(messageLog);
    }
}

