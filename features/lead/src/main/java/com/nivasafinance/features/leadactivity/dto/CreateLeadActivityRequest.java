package com.nivasafinance.features.leadactivity.dto;

import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLeadActivityRequest {
    private ResourceEnum resource;
    private ResourceAction action;
    private String description;
    private Map<String, Object> metadata;
    private Long resourceId;
    private Long leadId;
    private String createdBy;
}
