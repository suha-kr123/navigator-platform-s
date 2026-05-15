package com.nivasafinance.features.leadotp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactSendOtpResponse {
    private Long oneTimeTokenId;
    private String reference;
    private Integer validityInMins;
}
