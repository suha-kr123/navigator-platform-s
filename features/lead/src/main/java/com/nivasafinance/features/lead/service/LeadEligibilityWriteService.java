package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;

import java.util.UUID;

public interface LeadEligibilityWriteService {

    LeadBREResultExecuteResponse executeEligibility(UUID leadId);

    void executeEligibilityOnStageTransition(UUID leadIdentifier);
}
