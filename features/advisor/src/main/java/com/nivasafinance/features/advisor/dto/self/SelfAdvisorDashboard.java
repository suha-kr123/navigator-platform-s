package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfAdvisorDashboard {
    private String name;
    private String segmentationValue;
    private String salesOwner;
    private String salesOwnerMobile;
}
