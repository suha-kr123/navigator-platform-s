package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContactResponse {
    
    private UUID identifier;
    
    private LeadContactPersonDetails contactPersonDetails;
    
    private LeadContactPersonType applicantType;
    
    private Boolean isDecisionMaker;
    
    private Boolean isPropertyOwner;
}

