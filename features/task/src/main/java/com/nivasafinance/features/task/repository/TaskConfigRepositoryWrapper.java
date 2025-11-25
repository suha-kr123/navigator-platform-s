package com.nivasafinance.features.task.repository;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskConfigNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskConfigRepositoryWrapper {

    private final TaskConfigRepository taskConfigRepository;
    private final MessageSource messageSource;

    public TaskConfig findActiveByTaskConfigKey(String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(taskConfigKey)
                .orElseThrow(() -> new TaskConfigNotFoundException(taskConfigKey, messageSource));

        if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
            throw TaskOperationException.taskConfigInactive(taskConfigKey, messageSource);
        }

        return taskConfig;
    }

    public Map<String, TaskConfig> findActiveByTaskConfigKeys(List<String> taskConfigKeys) {
        if (!ValidationUtils.isNonNull(taskConfigKeys) || taskConfigKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TaskConfig> taskConfigs = taskConfigRepository
                .findByTaskConfigKeyInAndIsActiveTrue(taskConfigKeys);

        return taskConfigs.stream()
                .collect(Collectors.toMap(TaskConfig::getTaskConfigKey, config -> config));
    }
}

