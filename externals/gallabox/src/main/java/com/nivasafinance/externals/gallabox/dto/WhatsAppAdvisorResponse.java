package com.nivasafinance.externals.gallabox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppAdvisorResponse {
    private UUID advisorIdentifier;
    private String name;
    private String referralCode;
    private String status;
}
