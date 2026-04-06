package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadEligibilityWriteServiceImpl implements LeadEligibilityWriteService {

    private static final String ELIGIBILITY_CONFIG = "eligibility";

    private final LeadBREResultWriteService leadBREResultWriteService;

    @Override
    public LeadBREResultExecuteResponse executeEligibility(UUID leadId) {
        return leadBREResultWriteService.executeBre(leadId, ELIGIBILITY_CONFIG);
    }
}
