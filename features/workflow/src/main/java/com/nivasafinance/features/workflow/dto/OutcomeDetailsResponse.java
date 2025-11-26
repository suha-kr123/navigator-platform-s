package com.nivasafinance.features.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutcomeDetailsResponse {
    
    private String remarks;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime completedAt;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String completedBy;
    
    private String rescheduleReasonCodeValueKey;
}

