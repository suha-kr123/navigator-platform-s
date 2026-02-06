package com.nivasafinance.externals.exotel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutgoingCallbackRequest {
    
    @NotBlank(message = "callId is required")
    @JsonProperty("CallSid")
    private String callId;
}
