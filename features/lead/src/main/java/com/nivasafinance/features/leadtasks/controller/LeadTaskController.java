package com.nivasafinance.features.leadtasks.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.leadtasks.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadCompleteTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadReassignTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadRescheduleTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadTaskResponse;
import com.nivasafinance.features.leadtasks.service.LeadTaskReadService;
import com.nivasafinance.features.leadtasks.service.LeadTaskWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/tasks")
@AllArgsConstructor
public class LeadTaskController {

    private final LeadTaskReadService leadTaskReadService;
    private final LeadTaskWriteService leadTaskWriteService;

    @GetMapping
    @RequirePermission(permissionName = "READ_LEAD_TASKS")
    public ResponseEntity<PaginatedResponse<LeadTaskResponse>> getTasks(
            @PathVariable UUID leadId,
            @Valid PaginationRequest paginationRequest) {
        return ResponseEntity.ok(leadTaskReadService.getTasksByLeadId(leadId, paginationRequest));
    }

    @PostMapping("/complete")
    @RequirePermission(permissionName = "UPDATE_LEAD_TASKS")
    public ResponseEntity<LeadTaskResponse> completeTask(
            @PathVariable UUID leadId,
            @Valid @RequestBody LeadCompleteTaskRequest request) {
        LeadTaskResponse response = leadTaskWriteService.completeTask(leadId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reassign")
    @RequirePermission(permissionName = "UPDATE_LEAD_TASKS")
    public ResponseEntity<LeadTaskResponse> reassignTask(
            @PathVariable UUID leadId,
            @Valid @RequestBody LeadReassignTaskRequest request) {
        LeadTaskResponse response = leadTaskWriteService.reassignTask(leadId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reschedule")
    @RequirePermission(permissionName = "UPDATE_LEAD_TASKS")
    public ResponseEntity<LeadTaskResponse> rescheduleTask(
            @PathVariable UUID leadId,
            @Valid @RequestBody LeadRescheduleTaskRequest request) {
        LeadTaskResponse response = leadTaskWriteService.rescheduleTask(leadId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/adhoc")
    @RequirePermission(permissionName = "READ_LEAD_TASKS")
    public ResponseEntity<List<String>> getAvailableAdhocTasks(
            @PathVariable UUID leadId,
            @RequestParam(required = false) String stageKey) {
        List<String> adhocTaskKeys = leadTaskReadService.getAvailableAdhocTasks(leadId, stageKey);
        return ResponseEntity.ok(adhocTaskKeys);
    }

    @PostMapping("/adhoc")
    @RequirePermission(permissionName = "CREATE_LEAD_TASKS")
    public ResponseEntity<LeadTaskResponse> createAdhocTask(
            @PathVariable UUID leadId,
            @Valid @RequestBody CreateAdhocTaskRequest request) {
        LeadTaskResponse response = leadTaskWriteService.createAdhocTask(leadId, request);
        return ResponseEntity.ok(response);
    }
}
