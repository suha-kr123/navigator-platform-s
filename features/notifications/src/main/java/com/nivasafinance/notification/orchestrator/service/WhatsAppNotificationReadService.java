package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;

import java.util.List;

/**
 * Exposes read models for lead/advisor WhatsApp rows (analogue of {@code CallReadService} for call logs).
 */
public interface WhatsAppNotificationReadService {

    List<WhatsAppNotificationLogResponse> getLeadWhatsAppNotificationLogsByIds(List<Long> notificationIds);

    List<WhatsAppNotificationLogResponse> getAdvisorWhatsAppNotificationLogsByIds(List<Long> notificationIds);
}
