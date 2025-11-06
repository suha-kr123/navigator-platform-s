package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;

public interface TaskWriteService {

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse reassignTask(ReassignTaskRequest request);

    TaskResponse rescheduleTask(RescheduleTaskRequest request);

    TaskResponse completeTask(CompleteTaskRequest request);
}

