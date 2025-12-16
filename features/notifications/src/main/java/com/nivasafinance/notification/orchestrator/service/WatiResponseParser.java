package com.nivasafinance.notification.orchestrator.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Helper service to parse WATI API responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatiResponseParser {

    private final ObjectMapper objectMapper;
    private ObjectMapper lenientMapper;
    
    @PostConstruct
    public void init() {
        // Create a lenient ObjectMapper that ignores unknown properties
        lenientMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
    }

    /**
     * Parses raw WATI send template response JSON string.
     */
    public WatiSendTemplateResponse parseSendTemplateResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("Raw response is null or blank");
            return null;
        }
        
        log.info("Parsing WATI response. Length: {}, Content: {}", 
                rawResponse.length(), 
                rawResponse.length() > 1000 ? rawResponse.substring(0, 1000) + "..." : rawResponse);
        
        try {
            // Use lenient mapper to ignore unknown properties like "parameters"
            WatiSendTemplateResponse parsed = lenientMapper.readValue(rawResponse, WatiSendTemplateResponse.class);
            log.info("Successfully parsed WATI response: result={}, templateName={}, receivers={}", 
                    parsed.getResult(), parsed.getTemplateName(), 
                    parsed.getReceivers() != null ? parsed.getReceivers().size() : 0);
            
            if (parsed.getReceivers() != null && !parsed.getReceivers().isEmpty()) {
                WatiSendTemplateResponse.Receiver firstReceiver = parsed.getReceivers().get(0);
                log.info("First receiver: localMessageId={}, waId={}, isValidWhatsAppNumber={}", 
                        firstReceiver.getLocalMessageId(), firstReceiver.getWaId(), firstReceiver.getIsValidWhatsAppNumber());
            }
            
            return parsed;
        } catch (Exception ex) {
            log.error("Failed to parse WATI send template response. Full response content: {}", rawResponse, ex);
            return null;
        }
    }
}

