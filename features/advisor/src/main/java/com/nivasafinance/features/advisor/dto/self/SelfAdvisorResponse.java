package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.features.advisor.dto.OtherDetails;
import com.nivasafinance.features.advisor.dto.PersonalDetails;
import com.nivasafinance.features.advisor.dto.QualificationDetails;
import com.nivasafinance.features.advisor.dto.SegmentationDetails;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAdvisorResponse {
    private AdvisorStatus status;
    private PersonalDetails personalDetails;
    private QualificationDetails qualificationDetails;
    private OtherDetails occupationDetails;
    private SegmentationDetails segmentationDetails;
    private List<SelfAddressResponse> addressDetails;
    private List<SelfBankDetailsResponse> bankDetails;
    private SelfReferredByDetails referredByDetails;
}
