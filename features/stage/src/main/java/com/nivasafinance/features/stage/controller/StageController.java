package com.nivasafinance.features.stage.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.EntityOfficeKeyService;
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
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/stages")
@AllArgsConstructor
public class StageController {

    private final StageReadService stageReadService;
    private final EntityOfficeKeyService entityOfficeKeyService;

    @GetMapping
    @RequirePermission(permissionName = "READ_STAGE")
    public ResponseEntity<List<StageFilterResponse>> getAllActiveStages() {
        return ResponseEntity.ok(stageReadService.getAllActiveStages());
    }

    @PostMapping("/template")
    @RequirePermission(permissionName = "READ_STAGE")
    public ResponseEntity<Map<String, StageTemplateResponse>> getStageTemplates(
            @Valid @RequestBody StageKeysRequest request) {
        Map<String, StageTemplateResponse> templates = stageReadService.getStageTemplates(request.getStageKeys());
        return ResponseEntity.ok(templates != null ? templates : Collections.emptyMap());
    }

    
    @PostMapping("/assignable-users")
    @RequirePermission(permissionName = "READ_STAGE")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignableUsersForStages(
            @Valid @RequestBody StageKeysRequest request,
            @RequestParam EntityType entityType,
            @RequestParam UUID entityId) {
        // Get entity's office to determine which users to show
        String officeKey = entityOfficeKeyService.getOfficeKey(entityType, entityId);
        if (!ValidationUtils.isNonNull(officeKey)) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<UserAssignmentResponse> users = stageReadService.getAssignableUsersForStages(
                request.getStageKeys(), officeKey);
        return ResponseEntity.ok(users != null ? users : Collections.emptyList());
    }

    /**
     * Backward compatibility endpoint for getting assignable users for a single stage.
     * GET /api/v1/stages/{stageKey}/assignable-users
     */
    @GetMapping("/{stageKey}/assignable-users")
    @RequirePermission(permissionName = "READ_STAGE")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignableUsersForStage(
            @PathVariable String stageKey,
            @RequestParam EntityType entityType,
            @RequestParam UUID entityId) {
        // Get entity's office to determine which users to show
        String officeKey = entityOfficeKeyService.getOfficeKey(entityType, entityId);
        if (!ValidationUtils.isNonNull(officeKey)) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<UserAssignmentResponse> users = stageReadService.getAssignableUsersForStages(
                Collections.singletonList(stageKey), officeKey);
        return ResponseEntity.ok(users != null ? users : Collections.emptyList());
    }

    /**
     * Backward compatibility endpoint for getting template for a single stage.
     * GET /api/v1/stages/{stageKey}/template
     */
    @GetMapping("/{stageKey}/template")
    public ResponseEntity<StageTemplateResponse> getStageTemplate(
            @PathVariable String stageKey) {
        StageTemplateResponse template = stageReadService.getStageTemplate(stageKey);
        return ResponseEntity.ok(template);
    }
}

