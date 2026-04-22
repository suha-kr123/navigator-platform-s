package com.nivasafinance.features.leadqueues.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueConfigResponse {
    private String queueConfigName;
    private String description; 
}
