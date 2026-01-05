package com.nivasafinance.services.voice.provider.exotel.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExotelCreateCampaignResponse {
    @JsonProperty("request_id")
    private String requestId;
    
    private List<ResponseItem> response;
    
    public String getId() {
        if (response != null && !response.isEmpty() && response.get(0).getData() != null) {
            return response.get(0).getData().getId();
        }
        return null;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResponseItem {
        private ResponseData data;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResponseData {
        private String id;
    }
}
