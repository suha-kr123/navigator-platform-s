package com.nivasafinance.features.workflow.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/workflows")
@AllArgsConstructor
public class WorkflowController {

    private final WorkflowOrchestratorService workflowOrchestratorService;

    @GetMapping("/{workflowConfigKey}/stages/{stageKey}/adhoc-tasks")
    public ResponseEntity<List<String>> getAdhocTaskKeysForStage(
            @PathVariable String workflowConfigKey,
            @PathVariable String stageKey) {
        List<String> taskConfigKeys = workflowOrchestratorService.getAdhocTaskKeysForStage(workflowConfigKey, stageKey);
        return ResponseEntity.ok(taskConfigKeys);
    }
}

