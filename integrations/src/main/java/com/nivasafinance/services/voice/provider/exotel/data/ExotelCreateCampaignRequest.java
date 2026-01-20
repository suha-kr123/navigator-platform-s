package com.nivasafinance.services.voice.provider.exotel.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExotelCreateCampaignRequest {
    @JsonProperty("campaigns")
    private List<Campaign> campaigns;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Campaign {
        private String name;
        private String type; // "trans"
        @JsonProperty("campaign_type")
        private String campaignType; // "static"
        private String url; // app bazar flow url
        @JsonProperty("caller_id")
        private String callerId;
        private List<String> lists;
        @JsonProperty("status_callback")
        private String statusCallback;
        @JsonProperty("call_status_callback")
        private String callStatusCallback;
        @JsonProperty("call_schedule_callback")
        private String callScheduleCallback;
        @JsonProperty("call_duplicate_numbers")
        private Boolean callDuplicateNumbers;
        private String mode; // "auto" or "custom"
        @JsonProperty("throttle")
        private Integer throttle; // CPM value as integer
        private ExotelRetries retries;
        @JsonProperty("custom_field")
        private String customField;
        private Schedule schedule;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Schedule {
        @JsonProperty("send_at")
        private String sendAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExotelRetries {
        @JsonProperty("number_of_retries")
        private Integer noOfRetries;
        @JsonProperty("interval_mins")
        private Integer intervalMins;
        @JsonProperty("mechanism")
        private String mechanism;
        @JsonProperty("on_status")
        private List<String> onStatus;
    }
}
