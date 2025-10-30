package com.nivasafinance.features.leadlender.service;

import com.nivasafinance.features.leadlender.dto.LeadLenderResponse;

import java.util.List;
import java.util.UUID;

public interface LeadLenderReadService {
    
    List<LeadLenderResponse> getLeadLenders(UUID leadId, List<String> status);
    
    LeadLenderResponse getLeadLenderByIdentifier(UUID leadId, UUID lenderIdentifier);
}

