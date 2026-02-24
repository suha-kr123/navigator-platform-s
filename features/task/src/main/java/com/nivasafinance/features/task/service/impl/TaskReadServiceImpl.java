package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.dto.EntityContextResponse;
import com.nivasafinance.features.task.dto.TaskDetailsResponse;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.task.service.EntityContextEnricher;
import com.nivasafinance.features.task.service.TaskEntityService;
import com.nivasafinance.features.task.service.TaskEntityServiceFactory;
import com.nivasafinance.features.task.service.TaskReadService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, noRollbackFor = ResourceNotFoundException.class)
public class TaskReadServiceImpl implements TaskReadService {

    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final CodeValueMasterService codeValueMasterService;
    private final ApplicationContext applicationContext;
    private Map<EntityType, EntityContextEnricher> enricherMap;
    private final TaskEntityServiceFactory taskEntityServiceFactory;

    @Override
    public PaginatedResponse<TaskResponse> getTasksByAssignedTo(String assignedTo, boolean includeCompleted,
            PaginationRequest paginationRequest) {
        PaginatedResponse<TaskResponse> repoResponse = taskRepositoryWrapper.findTasksByAssignedToPaginated(assignedTo,
                includeCompleted, paginationRequest);
        List<TaskResponse> tasks = repoResponse.getContent();

        try {
            TaskReadServiceImpl selfProxy = applicationContext.getBean(TaskReadServiceImpl.class);
            selfProxy.enrichOutcomeValues(tasks);
            selfProxy.enrichEntityContext(tasks);
        } catch (Exception e) {
            // intentionally ignored
        }

        return PaginatedResponse.<TaskResponse>builder()
                .content(tasks)
                .pagination(repoResponse.getPagination())
                .build();
    }

    @Override
    public PaginatedResponse<TaskResponse> getAllTasks(EntityType entityType, UUID entityId,
            boolean includeCompleted, PaginationRequest paginationRequest) {
        if (ValidationUtils.isNonNull(entityType) && ValidationUtils.isNonNull(entityId)) {
            TaskEntityService taskEntityService = taskEntityServiceFactory.getTaskEntityService(entityType);
            taskEntityService.validate(entityId);
            return taskRepositoryWrapper.findTasksByEntityPaginated(entityType, entityId, includeCompleted, paginationRequest);
        }
        return taskRepositoryWrapper.findAllTasksPaginated(includeCompleted, paginationRequest);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void enrichEntityContext(List<TaskResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        for (TaskResponse response : responses) {
            if (response == null || response.getTaskDetails() == null) {
                continue;
            }

            TaskDetailsResponse taskDetails = response.getTaskDetails();
            EntityType entityType = taskDetails.getEntityType();
            UUID entityId = taskDetails.getEntityId();
            if (entityType == null || entityId == null) {
                continue;
            }
            try {
                Map<String, Object> entityData = enrichEntityDataByType(entityType, entityId);

                EntityContextResponse entityContext = EntityContextResponse.builder()
                        .entityType(entityType)
                        .entityIdentifier(entityId)
                        .entityData(entityData)
                        .build();

                response.setEntityContext(entityContext);
            } catch (Exception e) {
                // intentionally ignored
            }
        }
    }

    private Map<EntityType, EntityContextEnricher> getEnricherMap() {
        if (enricherMap == null) {
            Map<String, EntityContextEnricher> enricherBeans = applicationContext
                    .getBeansOfType(EntityContextEnricher.class);
            enricherMap = enricherBeans.values().stream()
                    .collect(Collectors.toMap(
                            EntityContextEnricher::getEntityType,
                            Function.identity()));
        }
        return enricherMap;
    }

    private Map<String, Object> enrichEntityDataByType(EntityType entityType, UUID entityId) {
        Map<EntityType, EntityContextEnricher> enrichers = getEnricherMap();
        EntityContextEnricher enricher = enrichers.get(entityType);

        if (enricher == null) {
            return new java.util.HashMap<>();
        }

        return enricher.enrichEntityData(entityId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void enrichOutcomeValues(List<TaskResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }

        for (TaskResponse response : responses) {
            if (response == null) {
                continue;
            }

            String outcomeKey = response.getOutcome();
            if (ValidationUtils.isNonNullOrEmpty(outcomeKey)) {
                try {
                    CodeValueResponse outcomeCodeValue = codeValueMasterService.getByKey(outcomeKey);
                    if (ValidationUtils.isNonNull(outcomeCodeValue)
                            && ValidationUtils.isNonNullOrEmpty(outcomeCodeValue.getValue())) {
                        response.setOutcome(outcomeCodeValue.getValue());
                    }
                } catch (ResourceNotFoundException e) {
                    // intentionally ignored
                } catch (RuntimeException e) {
                    // intentionally ignored
                } catch (Exception e) {
                    // intentionally ignored
                }
            }
        }
    }
}
