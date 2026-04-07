package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDashboardTaskSummary {
    private UUID taskIdentifier;
    private LocalDateTime dueAt;
    private String taskConfigKey;
    private String taskName;
}
