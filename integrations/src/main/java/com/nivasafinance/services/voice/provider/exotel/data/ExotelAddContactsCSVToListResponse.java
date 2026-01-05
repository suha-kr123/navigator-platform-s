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
public class ExotelAddContactsCSVToListResponse {
    private Response response;
    
    public String getUploadId() {
        if (response != null && response.getData() != null && response.getData().getSummary() != null) {
            return response.getData().getSummary().getUploadId();
        }
        return null;
    }
    
    public String getListSid() {
        if (response != null && response.getData() != null && response.getData().getSummary() != null) {
            return response.getData().getSummary().getListSid();
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
        private Summary summary;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        @JsonProperty("upload_id")
        private String uploadId;
        
        @JsonProperty("list_sid")
        private String listSid;
    }
}
