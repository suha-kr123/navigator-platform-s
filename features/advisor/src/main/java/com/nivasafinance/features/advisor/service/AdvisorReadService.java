package com.nivasafinance.features.advisor.service;

import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.AdvisorTemplateResponse;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;

import java.util.UUID;

public interface AdvisorReadService {
    
    AdvisorResponse getAdvisorByIdentifier(UUID identifier);
    
    SourcingDetailsResponse getSourcingDetails(UUID identifier);
    
    AdvisorTemplateResponse getAdvisorTemplate();
}
