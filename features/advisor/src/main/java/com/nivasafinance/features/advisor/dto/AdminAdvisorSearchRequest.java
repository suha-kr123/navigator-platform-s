package com.nivasafinance.features.advisor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin-only advisor search by mobile. No fixed digit-length validation (unlike {@link AdvisorSearchRequest} for CRM).
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminAdvisorSearchRequest {

    @NotBlank(message = "{validation.mobile.mandatory}")
    private String mobileNumber;
}
