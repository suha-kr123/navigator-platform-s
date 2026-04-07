package com.nivasafinance.features.workflow.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.dto.ExecutePendingActionRequest;
import com.nivasafinance.features.workflow.dto.PendingActionResponse;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import com.nivasafinance.features.workflow.service.PendingWorkflowActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/workflow/pending-actions")
@RequiredArgsConstructor
public class PendingWorkflowActionController {

    private final PendingWorkflowActionService pendingWorkflowActionService;
    private final WorkflowOrchestratorService workflowOrchestratorService;

    @GetMapping
    public ResponseEntity<List<PendingActionResponse>> getPendingActions(
            @RequestParam(required = false) UUID sourceTaskIdentifier,
            @RequestParam(required = false) UUID entityIdentifier,
            @RequestParam(required = false) EntityType entityType) {

        if (sourceTaskIdentifier != null) {
            return ResponseEntity.ok(
                    pendingWorkflowActionService.getPendingActionsBySourceTask(sourceTaskIdentifier));
        }

        if (entityIdentifier != null && entityType != null) {
            return ResponseEntity.ok(
                    pendingWorkflowActionService.getPendingActionsByEntity(entityIdentifier, entityType));
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/execute")
    public ResponseEntity<Void> executePendingAction(
            @Valid @RequestBody ExecutePendingActionRequest request) {
        workflowOrchestratorService.executePendingAction(
                request.getActionIdentifier(),
                request.getAssignTo(),
                request.getDueDate());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{actionIdentifier}/cancel")
    public ResponseEntity<Void> cancelPendingAction(
            @PathVariable UUID actionIdentifier) {
        workflowOrchestratorService.cancelPendingAction(actionIdentifier);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel-by-source-task")
    public ResponseEntity<Void> cancelPendingActionsBySourceTask(
            @RequestParam UUID sourceTaskIdentifier) {
        workflowOrchestratorService.cancelPendingActionsBySourceTask(sourceTaskIdentifier);
        return ResponseEntity.ok().build();
    }
}
