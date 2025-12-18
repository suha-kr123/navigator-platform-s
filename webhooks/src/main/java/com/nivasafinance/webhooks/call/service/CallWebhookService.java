package com.nivasafinance.webhooks.call.service;

import com.nivasafinance.webhooks.call.dto.CallNotificationResponse;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface CallWebhookService {
    Map<String, Object> handleWebhook(MultiValueMap<String, String> formData);
    
    void sendNotificationAsync(CallNotificationResponse notification, String userPhone);
}

