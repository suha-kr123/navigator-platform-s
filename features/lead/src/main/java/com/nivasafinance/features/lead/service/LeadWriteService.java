package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;

import java.util.UUID;

public interface LeadWriteService {
    CreateLeadResponse createLead(CreateLeadRequest request);
    void updatePreliminaryDetails(UUID leadIdentifier, UpdatePreliminaryDetailsRequest request);
}
