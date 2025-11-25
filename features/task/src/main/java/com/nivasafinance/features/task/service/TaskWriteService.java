package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.dto.BulkReassignTaskRequest;
import com.nivasafinance.features.task.dto.BulkReassignTaskResponse;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.UpdateDueDateRequest;

public interface TaskWriteService {

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse reassignTask(ReassignTaskRequest request);

    TaskResponse rescheduleTask(RescheduleTaskRequest request);

    TaskResponse completeTask(CompleteTaskRequest request);
    
    BulkReassignTaskResponse bulkReassignTasks(BulkReassignTaskRequest request);
    
    TaskResponse updateDueDate(UpdateDueDateRequest request);
}

