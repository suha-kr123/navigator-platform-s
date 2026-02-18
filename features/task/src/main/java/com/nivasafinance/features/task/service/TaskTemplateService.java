package com.nivasafinance.features.task.service;

import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.task.dto.TaskTemplateResponse;
import com.nivasafinance.common.enums.EntityType;
import java.util.List;
import java.util.UUID;

public interface TaskTemplateService {

    TaskTemplateResponse getTaskTemplate(String taskConfigKey);

    List<TaskTemplateResponse> getAdhocTasksTemplates(EntityType entityType, UUID entityId);

    List<UserAssignmentResponse> getAssignableUsersForTask(String taskConfigKey, EntityType entityType, UUID entityId);
}

