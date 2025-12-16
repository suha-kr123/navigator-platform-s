package com.nivasafinance.notification.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WATI webhook payload.
 * Handles multiple event types: sentMessageDELIVERED_v2, sentMessageREPLIED_v2, message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatiWebhookPayload {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("statusString")
    private String statusString;
    
    @JsonProperty("localMessageId")
    private String localMessageId;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("whatsappMessageId")
    private String whatsappMessageId;
    
    @JsonProperty("conversationId")
    private String conversationId;
    
    @JsonProperty("ticketId")
    private String ticketId;
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("assigneeId")
    private String assigneeId;
    
    @JsonProperty("operatorEmail")
    private String operatorEmail;
    
    @JsonProperty("waId")
    private String waId;
    
    @JsonProperty("senderName")
    private String senderName;
    
    @JsonProperty("replyContextId")
    private String replyContextId;
    
    @JsonProperty("buttonReply")
    private ButtonReply buttonReply;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ButtonReply {
        @JsonProperty("payload")
        private String payload;
        
        @JsonProperty("text")
        private String text;
    }
}

