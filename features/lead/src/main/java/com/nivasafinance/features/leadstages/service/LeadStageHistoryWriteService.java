package com.nivasafinance.features.leadstages.service;

import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentRequest;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentResponse;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;

import java.util.UUID;

public interface LeadStageHistoryWriteService {

    LeadStageHistory createInitialStage(UUID leadId, String workflowConfigKey);

    LeadStageHistory createStageEntry(UUID leadId, StageTransitionRequest request);

    LeadStageHistory changeAssignment(UUID leadId, String stageKey, String newAssignedTo);

    LeadStageHistory changeSubStage(UUID leadId, String stageKey, String subStageKey);
    
    BulkChangeAssignmentResponse bulkChangeAssignment(BulkChangeAssignmentRequest request);
}
