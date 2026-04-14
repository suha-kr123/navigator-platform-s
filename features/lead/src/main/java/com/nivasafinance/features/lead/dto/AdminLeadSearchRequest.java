package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin-only lead search by mobile. No fixed digit-length validation (unlike {@link LeadSearchRequest} for CRM).
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLeadSearchRequest {

    @NotBlank(message = "Mobile number is mandatory")
    private String mobileNumber;
}
