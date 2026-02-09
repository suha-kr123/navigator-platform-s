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
public class ExotelV3CallResponse {

    @JsonProperty("request_id")
    private String requestId;

    private String method;

    @JsonProperty("http_code")
    private Integer httpCode;

    private Object metadata;
    private InnerResponse response;

    public CallDetails getCallDetails() {
        return response != null ? response.getCallDetails() : null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InnerResponse {
        private Integer code;

        @JsonProperty("error_data")
        private Object errorData;

        private String status;

        @JsonProperty("call_details")
        private CallDetails callDetails;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CallDetails {
        private String sid;
        private String direction;

        @JsonProperty("virtual_number")
        private String virtualNumber;

        private String state;
        private String status;
    }
}
