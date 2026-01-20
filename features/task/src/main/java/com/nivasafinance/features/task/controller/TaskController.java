package com.nivasafinance.features.task.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.EntityOfficeKeyService;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.task.dto.BulkReassignTaskRequest;
import com.nivasafinance.features.task.dto.BulkReassignTaskResponse;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.TaskTemplateResponse;
import com.nivasafinance.features.task.dto.UpdateDueDateRequest;
import com.nivasafinance.features.task.dto.UpdateTaskNameRequest;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskReadService;
import com.nivasafinance.features.task.service.TaskTemplateService;
import com.nivasafinance.features.task.service.TaskWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskReadService taskReadService;
    private final TaskWriteService taskWriteService;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final UserQueryService userQueryService;
    private final TaskTemplateService taskTemplateService;
    private final EntityOfficeKeyService entityOfficeKeyService;

    @GetMapping("/assigned-to-me")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<PaginatedResponse<TaskResponse>> getTasksAssignedToMe(
            @RequestParam(defaultValue = "false") boolean includeCompleted,
            @Valid PaginationRequest paginationRequest) {
        String currentUsername = UserContext.getUsername();
        return ResponseEntity.ok(taskReadService.getTasksByAssignedTo(currentUsername, includeCompleted, paginationRequest));
    }

    @PostMapping("/bulk-reassign")
    @RequirePermission(permissionName = "UPDATE_TASK")
    public ResponseEntity<BulkReassignTaskResponse> bulkReassignTasks(
            @Valid @RequestBody BulkReassignTaskRequest request) {
        return ResponseEntity.ok(taskWriteService.bulkReassignTasks(request));
    }

    @GetMapping("/{taskConfigKey}/template")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<TaskTemplateResponse> getTaskTemplate(
            @PathVariable String taskConfigKey,
            @RequestParam(required = false) String officeKey) {
        return ResponseEntity.ok(taskTemplateService.getTaskTemplate(taskConfigKey, officeKey));
    }

    @GetMapping("/{taskConfigKey}/assignable-users")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignableUsersForTask(
            @PathVariable String taskConfigKey,
            @RequestParam EntityType entityType,
            @RequestParam UUID entityId) {
        TaskConfig taskConfig = taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
        List<String> allowedRoles = ValidationUtils.isNonNull(taskConfig.getTaskConfigDetails()) 
                ? taskConfig.getTaskConfigDetails().getAllowedRoles() 
                : Collections.emptyList();
        
        // Get entity's office to determine which users to show
        String officeKey = entityOfficeKeyService.getOfficeKey(entityType, entityId);
        if (!ValidationUtils.isNonNull(officeKey)) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        return ResponseEntity.ok(userQueryService.getUsersByOfficeAndRoles(allowedRoles, officeKey));
    }

    @PutMapping("/{taskIdentifier}/due-date")
    @RequirePermission(permissionName = "UPDATE_TASK")
    public ResponseEntity<TaskResponse> updateDueDate(
            @PathVariable UUID taskIdentifier,
            @Valid @RequestBody UpdateDueDateRequest request) {
        // Set taskIdentifier from path variable
        request.setTaskIdentifier(taskIdentifier);
        TaskResponse response = taskWriteService.updateDueDate(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskIdentifier}/name")
    @RequirePermission(permissionName = "UPDATE_TASK")
    public ResponseEntity<TaskResponse> updateTaskName(
            @PathVariable UUID taskIdentifier,
            @Valid @RequestBody UpdateTaskNameRequest request) {
        TaskResponse response = taskWriteService.updateTaskName(taskIdentifier, request);
        return ResponseEntity.ok(response);
    }

}

