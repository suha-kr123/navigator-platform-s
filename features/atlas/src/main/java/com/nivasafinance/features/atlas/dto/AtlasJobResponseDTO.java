package com.nivasafinance.features.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasJobResponseDTO {

    private UUID callLogIdentifier;

    private String jobId;

    private String status;

    private String summaryUrl;

    private String analysisUrl;

    private String transcriptUrl;

    private String error;
}
