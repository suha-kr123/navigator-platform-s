package com.nivasafinance.features.task.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.task.dto.BulkReassignTaskRequest;
import com.nivasafinance.features.task.dto.BulkReassignTaskResponse;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.TaskTemplateResponse;
import com.nivasafinance.features.task.dto.UpdateDueDateRequest;
import com.nivasafinance.features.task.dto.UpdateTaskNameRequest;
import com.nivasafinance.features.task.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.task.service.TaskReadService;
import com.nivasafinance.features.task.service.TaskTemplateService;
import com.nivasafinance.features.task.service.TaskWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping(ApiConstants.V1 + "/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskReadService taskReadService;
    private final TaskWriteService taskWriteService;
    private final TaskTemplateService taskTemplateService;

    @GetMapping("/assigned-to-me")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<PaginatedResponse<TaskResponse>> getTasksAssignedToMe(
            @RequestParam(defaultValue = "false") boolean includeCompleted,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dueDateTo,
            @Valid PaginationRequest paginationRequest) {
        String currentUsername = UserContext.getUsername();
        return ResponseEntity.ok(taskReadService.getTasksByAssignedTo(currentUsername, includeCompleted, dueDateFrom, dueDateTo, paginationRequest));
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
            @PathVariable String taskConfigKey) {
        return ResponseEntity.ok(taskTemplateService.getTaskTemplate(taskConfigKey));
    }

    @GetMapping("/{taskConfigKey}/assignable-users")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignableUsersForTask(
            @PathVariable String taskConfigKey,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) UUID entityId) {
        return ResponseEntity.ok(taskTemplateService.getAssignableUsersForTask(taskConfigKey, entityType, entityId));
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

    @GetMapping("/all")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<PaginatedResponse<TaskResponse>> getTasksForEntity(
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false, defaultValue = "false") boolean includeCompleted,
            @Valid PaginationRequest paginationRequest) {
        return ResponseEntity.ok(taskReadService.getAllTasks(entityType, entityId, includeCompleted, paginationRequest));
    }

    //complete task
    @PostMapping("/{taskIdentifier}/complete")
    @RequirePermission(permissionName = "UPDATE_TASK")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable UUID taskIdentifier,
            @Valid @RequestBody CompleteTaskRequest request) {
        request.setTaskIdentifier(taskIdentifier);
        TaskResponse response = taskWriteService.completeTask(request);
        return ResponseEntity.ok(response);
    }

    // reassign task
    @PostMapping("/{taskIdentifier}/reassign")
    @RequirePermission(permissionName = "UPDATE_TASK")
    public ResponseEntity<TaskResponse> reassignTask(
            @PathVariable UUID taskIdentifier,
            @Valid @RequestBody ReassignTaskRequest request) {
        request.setTaskIdentifier(taskIdentifier);
        TaskResponse response = taskWriteService.reassignTask(request);
        return ResponseEntity.ok(response);
    }

    // reschedule task
    @PostMapping("/{taskIdentifier}/reschedule")
    @RequirePermission(permissionName = "UPDATE_TASK")
    public ResponseEntity<TaskResponse> rescheduleTask(
            @PathVariable UUID taskIdentifier,
            @Valid @RequestBody RescheduleTaskRequest request) {
        request.setTaskIdentifier(taskIdentifier);
        TaskResponse response = taskWriteService.rescheduleTask(request);
        return ResponseEntity.ok(response);
    }

    // get adhoc tasks 
    @GetMapping("/adhoc")
    @RequirePermission(permissionName = "READ_TASK")
    public ResponseEntity<List<TaskTemplateResponse>> getAdhocTasksTemplates(
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) UUID entityId) {
        return ResponseEntity.ok(taskTemplateService.getAdhocTasksTemplates(entityType, entityId));
    }

    // post adhoc task
    @PostMapping("/adhoc")
    @RequirePermission(permissionName = "CREATE_TASK")
    public ResponseEntity<TaskResponse> createAdhocTask(
            @Valid @RequestBody CreateAdhocTaskRequest request) {
        TaskResponse response = taskWriteService.createAdhocTask(request);
        return ResponseEntity.ok(response);
    }

}
