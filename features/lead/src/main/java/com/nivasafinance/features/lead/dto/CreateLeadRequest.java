package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {

    private BigDecimal requestedLoanAmount;

    private MobileNumberDetails phoneNumber;

    private String product;

    private String officeKey;

    private SourcingChannelRequest sourcingChannelRequest;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MobileNumberDetails {
        @NotBlank(message = "Phone number is mandatory")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        private String mobileNumber;
        private boolean isWhatsapp;
    }
}


