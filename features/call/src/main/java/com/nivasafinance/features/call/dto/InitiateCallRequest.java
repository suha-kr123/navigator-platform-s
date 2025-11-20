package com.nivasafinance.features.call.dto;

import com.nivasafinance.common.enums.SystemEntities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateCallRequest {
    private String fromPhoneNumber;
    private String toPhoneNumber;
    private SystemEntities entity;
    private Long entityId;
    private String identifier; // Lead Identifier
    private String businessPurpose;
}
