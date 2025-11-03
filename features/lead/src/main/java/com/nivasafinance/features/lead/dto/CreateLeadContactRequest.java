package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadContactRequest {
    
    @NotNull(message = "Contact person details are required")
    @Valid
    private LeadContactPersonDetails contactPersonDetails;
    
    @Builder.Default
    private LeadContactPersonType applicantType = LeadContactPersonType.NONE;
    
    @Builder.Default
    private Boolean isDecisionMaker = false;
    
    @Builder.Default
    private Boolean isPropertyOwner = false;
}

