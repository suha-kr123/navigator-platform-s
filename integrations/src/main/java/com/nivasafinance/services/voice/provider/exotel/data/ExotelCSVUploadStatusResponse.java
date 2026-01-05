package com.nivasafinance.services.voice.provider.exotel.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExotelCSVUploadStatusResponse {
    @JsonProperty("request_id")
    private String requestId;
    
    private Response response;
    
    public String getStatus() {
        if (response != null && response.getData() != null) {
            return response.getData().getStatus();
        }
        return null;
    }
    
    public Stats getStats() {
        if (response != null && response.getData() != null) {
            return response.getData().getStats();
        }
        return null;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private ResponseData data;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResponseData {
        @JsonProperty("upload_id")
        private String uploadId;
        
        @JsonProperty("list_sid")
        private String listSid;
        
        private String status;
        
        private Stats stats;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Stats {
        private Integer duplicate;
        private Integer total;
        private Integer success;
        private Integer failed;
    }
}
