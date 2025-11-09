package com.nivasafinance.features.leadactivity.dto;

import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadActivityResponse {
    private UUID identifier;
    private ResourceEnum resource;
    private ResourceAction action;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}


