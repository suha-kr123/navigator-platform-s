package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadResponse;

import java.util.UUID;

public interface LeadReadService {
    LeadResponse getLeadByIdentifier(UUID leadIdentifier);
}
