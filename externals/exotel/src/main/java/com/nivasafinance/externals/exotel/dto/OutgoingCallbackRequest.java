package com.nivasafinance.externals.exotel.dto;

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
    private String callId;
}
