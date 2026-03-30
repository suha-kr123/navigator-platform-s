package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadBREResultResponse;

import java.util.List;
import java.util.UUID;

public interface LeadBREResultReadService {

    List<LeadBREResultResponse> getResults(UUID leadId);

    List<LeadBREResultResponse> getResults(UUID leadId, String config);

    LeadBREResultResponse getResultByIdentifier(UUID leadId, UUID identifier);
}
