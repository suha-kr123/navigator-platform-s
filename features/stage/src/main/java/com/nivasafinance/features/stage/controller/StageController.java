package com.nivasafinance.features.stage.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageKeysRequest;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;
import com.nivasafinance.features.stage.service.StageReadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConstants.V1 + "/stages")
@AllArgsConstructor
public class StageController {

    private final StageReadService stageReadService;

    @GetMapping
    public ResponseEntity<List<StageFilterResponse>> getAllActiveStages() {
        return ResponseEntity.ok(stageReadService.getAllActiveStages());
    }

    @PostMapping("/template")
    public ResponseEntity<Map<String, StageTemplateResponse>> getStageTemplates(
            @Valid @RequestBody StageKeysRequest request) {
        Map<String, StageTemplateResponse> templates = stageReadService.getStageTemplates(request.getStageKeys());
        return ResponseEntity.ok(templates != null ? templates : Collections.emptyMap());
    }

    @PostMapping("/assignable-users")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignableUsersForStages(
            @Valid @RequestBody StageKeysRequest request) {
        List<UserAssignmentResponse> users = stageReadService.getAssignableUsersForStages(request.getStageKeys());
        return ResponseEntity.ok(users != null ? users : Collections.emptyList());
    }
}

