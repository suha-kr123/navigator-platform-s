package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadTemplateResponse {
    
    private List<CodeValueResponse> leadRejectionReasons;
    
    private List<CodeValueResponse> leadWithdrawalReasons;
    
    private List<CodeValueResponse> leadOnholdReasons;

    private List<CodeValueResponse> leadDropoffReasons;
    
    private List<CodeValueResponse> occupationProfiles;
    
    private List<CodeValueResponse> roofProfiles;
    
    private List<CodeValueResponse> ltvOptions;
    
    private List<CodeValueResponse> foirOptions;
    
    private List<CodeValueResponse> monthlyIncomes;
    
    private List<CodeValueResponse> propertyDocumentTypes;
    
    private List<CodeValueResponse> locations;
    
    private List<CodeValueResponse> bureauRatings;
    
    private List<CodeValueResponse> customerProfiles;

    private List<CodeValueResponse> leadPurposes;

    private List<CodeValueResponse> customerConvinceStatuses;

    private List<CodeValueResponse> priorities;
}

