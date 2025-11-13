package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorResponse {
    
    private UUID identifier;
    private AdvisorStatus status;
    private PersonalDetails personalDetails;
    private AdvisorRemarks remarks;
    private QualificationDetails qualificationDetails;
    private OtherDetails otherDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}

