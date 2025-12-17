package com.nivasafinance.services.voice.webhook;

import com.nivasafinance.services.voice.webhook.dto.CallNotificationResponse;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface VoiceWebhookHandler {
    
    String getProviderName();
    
    Map<String, Object> handleWebhook(MultiValueMap<String, String> formData);
    
    void sendNotificationAsync(CallNotificationResponse notification, String userPhone);
}

