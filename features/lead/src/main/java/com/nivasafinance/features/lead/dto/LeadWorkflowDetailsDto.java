package com.nivasafinance.features.lead.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadWorkflowDetailsDto {

    private Long leadId;
    private UUID leadIdentifier;
    private String workflowConfigKey;
    private String currentStageKey;
    private String currentSubStageKey;
    private String currentStageAssignedTo;
}
