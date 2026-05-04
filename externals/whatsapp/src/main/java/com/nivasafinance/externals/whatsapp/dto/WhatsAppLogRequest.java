package com.nivasafinance.externals.whatsapp.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppLogRequest {

    private String id;
    private String conversationId;
    private String channelId;
    private String localMessageId;
    private JsonNode whatsapp;
    private JsonNode contact;
}