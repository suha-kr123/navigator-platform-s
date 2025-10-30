package com.nivasafinance.features.leadlender.service;

import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest;

import java.util.UUID;

public interface LeadLenderWriteService {
    
    CreateLeadLenderResponse createLeadLender(UUID leadId, CreateLeadLenderRequest request);
    
    void updateLeadLender(UUID leadId, UUID lenderIdentifier, UpdateLeadLenderRequest request);
    
    void rejectLeadLender(UUID leadId, UUID lenderIdentifier, RejectLeadLenderRequest request);
}

