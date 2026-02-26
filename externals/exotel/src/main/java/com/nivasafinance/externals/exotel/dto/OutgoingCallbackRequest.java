package com.nivasafinance.externals.exotel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutgoingCallbackRequest {
    
    @NotNull(message = "call details required")
    @JsonProperty("call_details")
    private CallDetails callDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallDetails {
        private String sid;
    }
}
