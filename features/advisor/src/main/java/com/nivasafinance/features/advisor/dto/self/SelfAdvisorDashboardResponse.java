package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfAdvisorDashboardResponse {
    private String advisorName;
    private String salesOwner;
    private String salesOwnerMobile;
    private List<LeadStatusCount> leadCounts;
}
