package com.nivasafinance.features.leadlender.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadLenderRequest {
    
    private String lenderOfficeKey;
    
    @Valid
    private RmDetails rmDetails;
    
    @Valid
    private LoginDetails loginDetails;
    
    @Valid
    private ApprovedDetails approvedDetails;
    
    private String stage;
    
    private String remarks;
}
