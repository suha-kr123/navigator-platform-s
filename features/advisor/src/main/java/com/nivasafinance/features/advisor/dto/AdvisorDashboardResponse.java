package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvisorDashboardResponse {
    private UUID advisorId;
    private String name;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime lastLeadAt;
    private AdvisorStatus status;
    private String office;
    private CodeValueResponse segmentation;
    private CodeValueResponse sourcingChannel;
    private Long noOfLeads;
    private String salesOwner;
}

