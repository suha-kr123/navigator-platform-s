package com.nivasafinance.features.task.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.exception.TaskOperationException;

@Service
@RequiredArgsConstructor
public class TaskEntityServiceFactory {
    private final MessageSource messageSource;
    private final List<TaskEntityService> taskEntityServices;

    public TaskEntityService getTaskEntityService(EntityType entityType) {
        return taskEntityServices.stream().filter(service -> service.getEntityType() == entityType).findFirst()
                .orElseThrow(() -> TaskOperationException.entityServiceNotFound(entityType, messageSource));
    }
}
