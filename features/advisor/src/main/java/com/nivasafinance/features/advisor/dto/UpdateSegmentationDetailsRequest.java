package com.nivasafinance.features.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating segmentation details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSegmentationDetailsRequest {
    private String segmentation;
}

