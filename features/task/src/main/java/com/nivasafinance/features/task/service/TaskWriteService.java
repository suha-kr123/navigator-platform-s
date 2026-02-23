package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.dto.BulkReassignTaskRequest;
import com.nivasafinance.features.task.dto.BulkReassignTaskResponse;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.UpdateDueDateRequest;
import com.nivasafinance.features.task.dto.UpdateTaskNameRequest;

import java.util.UUID;

public interface TaskWriteService {

    TaskResponse createTask(CreateTaskRequest request);
    
    TaskResponse createAdhocTask(CreateAdhocTaskRequest request);

    TaskResponse reassignTask(ReassignTaskRequest request);

    TaskResponse rescheduleTask(RescheduleTaskRequest request);

    TaskResponse completeTask(CompleteTaskRequest request);
    
    BulkReassignTaskResponse bulkReassignTasks(BulkReassignTaskRequest request);
    
    TaskResponse updateDueDate(UpdateDueDateRequest request);
    
    TaskResponse updateTaskName(UUID taskIdentifier, UpdateTaskNameRequest request);
    
    void closeAllOpenTasksForLead(UUID leadIdentifier, String outcome);
}
