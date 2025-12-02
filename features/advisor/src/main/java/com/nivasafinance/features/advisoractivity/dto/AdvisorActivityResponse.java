package com.nivasafinance.features.advisoractivity.dto;

import com.nivasafinance.features.advisoractivity.enums.ResourceAction;
import com.nivasafinance.features.advisoractivity.enums.ResourceEnum;
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
public class AdvisorActivityResponse {
    private UUID identifier;
    private ResourceEnum resource;
    private ResourceAction action;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}

