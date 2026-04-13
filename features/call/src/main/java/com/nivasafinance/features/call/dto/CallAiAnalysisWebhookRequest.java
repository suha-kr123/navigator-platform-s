package com.nivasafinance.features.call.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallAiAnalysisWebhookRequest {

    private String id;

    @JsonProperty("agent_id")
    private String agentId;

    private String summary;

    @JsonProperty("extracted_data")
    private JsonNode extractedData;

    @JsonProperty("telephony_data")
    private TelephonyData telephonyData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelephonyData {
        @JsonProperty("provider_call_id")
        private String providerCallId;
    }
}
