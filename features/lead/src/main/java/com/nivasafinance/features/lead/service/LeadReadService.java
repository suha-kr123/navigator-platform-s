package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreditDetailsResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.PropertyDetailsResponse;
import com.nivasafinance.features.lead.dto.ProposedDetailsResponse;
import com.nivasafinance.features.lead.dto.SourcingDetailsResponse;

import java.util.UUID;

public interface LeadReadService {
    LeadResponse getLeadByIdentifier(UUID leadIdentifier);
    PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier);
    CreditDetailsResponse getCreditDetails(UUID leadIdentifier);
    ProposedDetailsResponse getProposedDetails(UUID leadIdentifier);
    PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier);
    SourcingDetailsResponse getSourcingDetails(UUID leadIdentifier);
}
