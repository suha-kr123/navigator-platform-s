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
public class ExotelGetCallStatusResponse {

    private Response response;

    public CallDetails getCallDetails() {
        return response != null ? response.getCallDetails() : null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
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
        private String legs;

        @JsonProperty("created_time")
        private String createdTime;

        @JsonProperty("updated_time")
        private String updatedTime;

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("end_time")
        private String endTime;

        @JsonProperty("total_duration")
        private Integer totalDuration;

        @JsonProperty("total_talk_time")
        private Integer totalTalkTime;

        @JsonProperty("custom_field")
        private String customField;

        @JsonProperty("app_id")
        private String appId;

        @JsonProperty("app_name")
        private String appName;

        private String digits;
        private List<Recording> recordings;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Recording {
        private String url;
    }
}
