package com.nivasafinance.features.advisor.task;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskEntityService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdvisorEntityService implements TaskEntityService {

    private final AdvisorReadService advisorReadService;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final MessageSource messageSource;

    @Override
    public EntityType getEntityType() {
        return EntityType.ADVISOR;
    }

    @Override
    public void validate(UUID entityId) {
        advisorReadService.getAdvisorByIdentifier(entityId);
    }

    @Override
    public boolean canCreateAdhocTask(TaskDetailsRequest taskDetails, String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
        if (ValidationUtils.isNonNull(taskConfig.getTaskConfigDetails())
                && Boolean.TRUE.equals(taskConfig.getTaskConfigDetails().getIsAdhocTaskAllowed())) {
            return true;
        }
        throw TaskOperationException.adhocTaskNotAllowed(taskConfigKey, messageSource);
    }

    @Override
    public List<String> getAdhocTasksTemplates(UUID entityId) {
        advisorReadService.getAdvisorByIdentifier(entityId);
        return taskConfigRepositoryWrapper.findActiveAdhocTasks().stream()
                .map(TaskConfig::getTaskConfigKey)
                .collect(Collectors.toList());
    }
}
