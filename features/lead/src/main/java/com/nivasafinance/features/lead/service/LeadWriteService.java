package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.UpdateCreditDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateProposedDetailsRequest;

import java.util.UUID;

public interface LeadWriteService {
    CreateLeadResponse createLead(CreateLeadRequest request);
    void updatePreliminaryDetails(UUID leadIdentifier, UpdatePreliminaryDetailsRequest request);
    void updateCreditDetails(UUID leadIdentifier, UpdateCreditDetailsRequest request);
    void updateProposedDetails(UUID leadIdentifier, UpdateProposedDetailsRequest request);
}
