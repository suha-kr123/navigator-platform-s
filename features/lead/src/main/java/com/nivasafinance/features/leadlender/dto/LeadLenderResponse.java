package com.nivasafinance.features.leadlender.dto;

import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadLenderResponse {
    
    private UUID lenderIdentifier;
    private UUID leadIdentifier;
    private LeadLenderStatus status;
    private LenderResponseData lender;
    private LenderOfficeReponseData lenderOffice;
    private LoginDetails loginDetails;
    private RmDetails relationshipManager;
    private ApprovedDetails approvedDetails;
    private StageResponse stage;
    private RemarksResponse remarks;
}
