package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.features.advisor.dto.MobileNumberDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAdvisorProfileRequest {
    private MobileNumberDetails mobileNumberDetails;
    private Optional<SelfPersonalDetailsRequest> personalDetails;
    private Optional<SelfQualificationDetailsRequest> qualificationDetails;
    private Optional<SelfOccupationDetailsRequest> occupationDetails;
   
}
