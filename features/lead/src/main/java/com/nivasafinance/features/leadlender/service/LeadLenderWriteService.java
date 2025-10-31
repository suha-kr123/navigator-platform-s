package com.nivasafinance.features.leadlender.service;

import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest;

import java.util.UUID;

public interface LeadLenderWriteService {
    
    CreateLeadLenderResponse createLeadLender(UUID leadIdentifier, CreateLeadLenderRequest request);
    
    void updateLeadLender(UUID leadIdentifier, UUID lenderIdentifier, UpdateLeadLenderRequest request);
    
    void rejectLeadLender(UUID leadIdentifier, UUID lenderIdentifier, RejectLeadLenderRequest request);
}

