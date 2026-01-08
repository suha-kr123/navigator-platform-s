package com.nivasafinance.services.voice.provider.exotel.data;

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
public class ExotelGetCampaignResponse {
    private List<ResponseItem> response;
    
    public CampaignData getCampaignData() {
        if (response != null && !response.isEmpty() && response.get(0).getData() != null) {
            return response.get(0).getData();
        }
        return null;
    }
    
    public Summary getSummary() {
        if (response != null && !response.isEmpty() && response.get(0).getSummary() != null) {
            return response.get(0).getSummary();
        }
        return null;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResponseItem {
        private CampaignData data;
        private Summary summary;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CampaignData {
        private String id;
        private String status;
        
        @JsonProperty("report_url")
        private String reportUrl;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        @JsonProperty("call_scheduled")
        private Long callScheduled;
        
        @JsonProperty("call_initialized")
        private Long callInitialized;
        
        @JsonProperty("call_completed")
        private Long callCompleted;
        
        @JsonProperty("call_failed")
        private Long callFailed;
        
        @JsonProperty("call_inprogress")
        private Long callInProgress;
    }
}

