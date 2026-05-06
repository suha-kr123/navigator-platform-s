package com.nivasafinance.externals.gallabox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppLeadRequest {

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNumber;

    private boolean isWhatsapp;

    private String sourcing_channel_name;

    @Valid
    private MarketingDetails marketing_details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketingDetails {
        private String sourceId;
        private String sourceUrl;
    }
}
