package com.nivasafinance.features.workflow.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.dto.PendingActionResponse;
import com.nivasafinance.features.workflow.dto.PostTaskAction;
import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import com.nivasafinance.features.workflow.repository.PendingWorkflowActionRepositoryWrapper;
import com.nivasafinance.features.workflow.service.PendingWorkflowActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PendingWorkflowActionServiceImpl implements PendingWorkflowActionService {

    private final PendingWorkflowActionRepositoryWrapper pendingActionRepositoryWrapper;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final StageConfigRepositoryWrapper stageConfigRepositoryWrapper;

    @Override
    public List<PendingActionResponse> getPendingActionsBySourceTask(UUID sourceTaskIdentifier) {
        List<PendingWorkflowAction> actions = pendingActionRepositoryWrapper
                .findPendingBySourceTask(sourceTaskIdentifier);
        return actions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PendingActionResponse> getPendingActionsByEntity(UUID entityIdentifier, EntityType entityType) {
        List<PendingWorkflowAction> actions = pendingActionRepositoryWrapper
                .findPendingByEntity(entityIdentifier, entityType);
        return actions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PendingActionResponse toResponse(PendingWorkflowAction action) {
        PendingWorkflowAction.ActionDetails details = action.getActionDetails();
        String actionType = details.getType();

        PendingActionResponse.PendingActionResponseBuilder builder = PendingActionResponse.builder()
                .actionIdentifier(action.getActionIdentifier())
                .actionType(actionType)
                .entityIdentifier(action.getEntityIdentifier())
                .entityType(action.getEntityType())
                .currentStageKey(action.getCurrentStageKey())
                .sourceTaskIdentifier(action.getSourceTaskIdentifier())
                .sourceTaskConfigKey(action.getSourceTaskConfigKey())
                .sourceOutcome(action.getSourceOutcome())
                .createdAt(action.getCreatedAt());

        if (PostTaskAction.TYPE_CREATE_TASK.equals(actionType)) {
            enrichWithTaskMetadata(builder, details.getTaskConfigKey());
        } else if (PostTaskAction.TYPE_MOVE_STAGE.equals(actionType)) {
            enrichWithStageMetadata(builder, details.getTargetStageKey());
        } else if (PostTaskAction.TYPE_CLOSE_TASKS.equals(actionType)) {
            builder.fields(PendingActionResponse.FieldRequirements.builder()
                    .assignTo(PendingActionResponse.FieldMeta.builder().required(false).build())
                    .dueDate(PendingActionResponse.FieldMeta.builder().required(false).build())
                    .build());
        } else if (PostTaskAction.TYPE_CHANGE_SUBSTAGE.equals(actionType)) {
            builder.targetSubStageKey(details.getTargetSubStageKey());
            builder.fields(PendingActionResponse.FieldRequirements.builder()
                    .assignTo(PendingActionResponse.FieldMeta.builder().required(false).build())
                    .dueDate(PendingActionResponse.FieldMeta.builder().required(false).build())
                    .build());
        }

        return builder.build();
    }

    private void enrichWithTaskMetadata(PendingActionResponse.PendingActionResponseBuilder builder,
            String taskConfigKey) {
        builder.taskConfigKey(taskConfigKey);

        List<String> allowedRoles = List.of();
        try {
            TaskConfig taskConfig = taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
            builder.taskName(taskConfig.getName());
            if (ValidationUtils.isNonNull(taskConfig.getTaskConfigDetails())
                    && ValidationUtils.isNonNull(taskConfig.getTaskConfigDetails().getAllowedRoles())) {
                allowedRoles = taskConfig.getTaskConfigDetails().getAllowedRoles();
            }
        } catch (Exception e) {
            builder.taskName(taskConfigKey);
        }

        builder.fields(PendingActionResponse.FieldRequirements.builder()
                .assignTo(PendingActionResponse.FieldMeta.builder()
                        .required(true)
                        .allowedRoles(allowedRoles)
                        .build())
                .dueDate(PendingActionResponse.FieldMeta.builder()
                        .required(true)
                        .build())
                .build());
    }

    private void enrichWithStageMetadata(PendingActionResponse.PendingActionResponseBuilder builder,
            String targetStageKey) {
        builder.targetStageKey(targetStageKey);

        List<String> assigneeRoles = List.of();
        try {
            StageConfig stageConfig = stageConfigRepositoryWrapper.findByKeyWithException(targetStageKey);
            builder.targetStageName(stageConfig.getName());
            if (ValidationUtils.isNonNull(stageConfig.getAssigneeRoles())
                    && ValidationUtils.isNonNull(stageConfig.getAssigneeRoles().getRoles())) {
                assigneeRoles = stageConfig.getAssigneeRoles().getRoles();
            }
        } catch (Exception e) {
            builder.targetStageName(targetStageKey);
        }

        builder.fields(PendingActionResponse.FieldRequirements.builder()
                .assignTo(PendingActionResponse.FieldMeta.builder()
                        .required(true)
                        .allowedRoles(assigneeRoles)
                        .build())
                .dueDate(PendingActionResponse.FieldMeta.builder()
                        .required(false)
                        .build())
                .build());
    }
}
