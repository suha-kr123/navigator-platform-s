package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfAdvisorDashboardResponse {
    private String advisorName;
    private String segmentation;
    private String salesOwner;
    private String salesOwnerMobile;
    private long totalLeads;
    private Map<String, Long> leadsCountByStage;
}
