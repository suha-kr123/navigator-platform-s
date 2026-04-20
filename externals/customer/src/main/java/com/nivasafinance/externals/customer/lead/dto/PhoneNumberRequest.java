package com.nivasafinance.externals.customer.lead.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneNumberRequest {

    @NotBlank
    private String number;
    private Boolean isPrimary;
    private Boolean isWhatsappAvailable;
}
