package com.nivasafinance.features.task.service;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;

import java.util.UUID;
import java.util.List;

public interface TaskEntityService {

    EntityType getEntityType();

    void validate(UUID entityId);
    
    boolean canCreateAdhocTask(TaskDetailsRequest taskDetails, String taskConfigKey);

    List<String> getAdhocTasksTemplates(UUID entityId);

}
