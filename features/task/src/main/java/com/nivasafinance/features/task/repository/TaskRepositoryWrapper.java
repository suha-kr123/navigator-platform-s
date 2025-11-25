package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.exception.TaskNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskRepositoryWrapper {

    private final TaskRepository taskRepository;
    private final MessageSource messageSource;

    public Task saveWithException(Task task) {
        try {
            return taskRepository.save(task);
        } catch (RuntimeException e) {
            throw TaskOperationException.taskConfigInactive(task.getTaskConfigKey(), messageSource);
        }
    }

    public Task findByTaskIdentifierWithException(UUID taskIdentifier) {
        return taskRepository.findByTaskIdentifier(taskIdentifier)
                .orElseThrow(() -> TaskNotFoundException.taskNotFoundByUuid(taskIdentifier, messageSource));
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }
}

