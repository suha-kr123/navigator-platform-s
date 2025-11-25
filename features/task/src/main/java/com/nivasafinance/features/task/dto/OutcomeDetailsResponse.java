package com.nivasafinance.features.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
        
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
}

