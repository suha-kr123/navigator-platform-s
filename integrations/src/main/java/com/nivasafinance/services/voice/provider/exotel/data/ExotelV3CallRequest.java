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
public class ExotelV3CallRequest {

    private ContactUri from;
    private ContactUri to;
    private Recording recording;

    @JsonProperty("virtual_number")
    private String virtualNumber;

    @JsonProperty("custom_field")
    private String customField;

    @JsonProperty("status_callback")
    private List<StatusCallback> statusCallback;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ContactUri {
        @JsonProperty("contact_uri")
        private String contactUri;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Recording {
        private boolean record;
        private String channels;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StatusCallback {
        private String event;
        private String url;
    }
}
