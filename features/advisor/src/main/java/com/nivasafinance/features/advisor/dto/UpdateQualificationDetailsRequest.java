package com.nivasafinance.features.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating qualification details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQualificationDetailsRequest {
    private String highestQualification;
}

