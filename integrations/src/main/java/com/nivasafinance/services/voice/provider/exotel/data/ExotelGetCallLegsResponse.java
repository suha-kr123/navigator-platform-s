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
public class ExotelGetCallLegsResponse {

    private Response response;

    public LegDetails getLegDetails() {
        return response != null ? response.getLegDetails() : null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        @JsonProperty("leg_details")
        private LegDetails legDetails;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LegDetails {
        @JsonProperty("account_sid")
        private String accountSid;

        @JsonProperty("call_sid")
        private String callSid;

        private List<Leg> from;
        private List<Leg> to;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Leg {
        private String sid;
        private String name;

        @JsonProperty("contact_uri")
        private String contactUri;

        @JsonProperty("date_created")
        private String dateCreated;

        @JsonProperty("date_updated")
        private String dateUpdated;

        private String status;

        @JsonProperty("group_id")
        private String groupId;

        @JsonProperty("group_name")
        private String groupName;
    }
}
