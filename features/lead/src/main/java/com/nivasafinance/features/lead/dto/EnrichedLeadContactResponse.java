package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedLeadContactResponse {
    
    private UUID identifier;
    
    private LeadContactPersonDetails contactPersonDetails;
    
    private LeadContactPersonType applicantType;
    
    private Boolean isDecisionMaker;
    
    private Boolean isPropertyOwner;
    
    @Builder.Default
    private List<AddressData> addresses = new ArrayList<>();
    
    @Builder.Default
    private List<IdentifierData> identifiers = new ArrayList<>();
}

