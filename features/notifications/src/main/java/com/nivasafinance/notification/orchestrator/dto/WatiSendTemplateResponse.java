package com.nivasafinance.notification.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for WATI send template API response.
 * Example response:
 * {
 *   "result": true,
 *   "error": null,
 *   "templateName": "construction_pending_on_hold_25_07_25",
 *   "receivers": [
 *     {
 *       "localMessageId": "82710d4b-2d8f-49d1-a448-cc9c192e31d4",
 *       "waId": "917975610193",
 *       "isValidWhatsAppNumber": true,
 *       "errors": []
 *     }
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields in response
public class WatiSendTemplateResponse {
    
    @JsonProperty("result")
    private Boolean result;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("templateName")
    private String templateName;
    
    @JsonProperty("receivers")
    private List<Receiver> receivers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields in receiver
    public static class Receiver {
        @JsonProperty("localMessageId")
        private String localMessageId;
        
        @JsonProperty("waId")
        private String waId;
        
        @JsonProperty("isValidWhatsAppNumber")
        private Boolean isValidWhatsAppNumber;
        
        @JsonProperty("errors")
        private List<String> errors;
    }
}

