package com.nivasafinance.features.leadstages.service;

import com.nivasafinance.features.leadstages.dto.CreateLeadStageHistoryRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;

public interface LeadStageHistoryWriteService {

    LeadStageHistory createStageEntry(CreateLeadStageHistoryRequest request);

    LeadStageHistory changeAssignment(Long leadId, String stageKey, String newAssignedTo);

    LeadStageHistory changeSubStage(Long leadId, String stageKey, String subStageKey);
}
