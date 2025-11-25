package com.nivasafinance.features.leadtasks.service;

import com.nivasafinance.features.leadtasks.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadCompleteTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadReassignTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadRescheduleTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadTaskResponse;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import java.util.Map;
import java.util.UUID;

public interface LeadTaskWriteService {
    
    LeadTaskResponse createAdhocTask(UUID leadIdentifier, CreateAdhocTaskRequest request);
    
    LeadTaskResponse createTaskAndAssociateWithLead(Long leadId, CreateTaskRequest request, Map<String, Object> taskDetails);
    
    LeadTaskResponse completeTask(UUID leadIdentifier, LeadCompleteTaskRequest request);
    
    LeadTaskResponse reassignTask(UUID leadIdentifier, LeadReassignTaskRequest request);
    
    LeadTaskResponse rescheduleTask(UUID leadIdentifier, LeadRescheduleTaskRequest request);
}

