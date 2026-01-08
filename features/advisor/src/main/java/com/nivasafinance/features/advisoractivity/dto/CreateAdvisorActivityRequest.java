package com.nivasafinance.features.advisoractivity.dto;

import com.nivasafinance.features.advisoractivity.enums.ResourceAction;
import com.nivasafinance.features.advisoractivity.enums.ResourceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAdvisorActivityRequest {
    private ResourceEnum resource;
    private ResourceAction action;
    private String description;
    private Map<String, Object> metadata;
    private Long resourceId;
    private Long advisorId;
    private String createdBy;
}

