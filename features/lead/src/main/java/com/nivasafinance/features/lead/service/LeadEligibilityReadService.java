package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadBREEligibilityDetailResponse;
import com.nivasafinance.features.lead.dto.LeadEligibilityResponse;

import java.util.Optional;
import java.util.UUID;

public interface LeadEligibilityReadService {

    Optional<LeadEligibilityResponse> getLatestEligibility(UUID leadId);

    Optional<LeadBREEligibilityDetailResponse> getLatestEligibilityDetail(UUID leadId);
}
