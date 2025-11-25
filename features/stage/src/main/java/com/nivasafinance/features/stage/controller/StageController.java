package com.nivasafinance.features.stage.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;
import com.nivasafinance.features.stage.service.StageReadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/stages")
@AllArgsConstructor
public class StageController {

    private final StageReadService stageReadService;
    private final UserQueryService userQueryService;

    @GetMapping("/{stageKey}/template")
    public ResponseEntity<StageTemplateResponse> getStageTemplate(@PathVariable String stageKey) {
        return ResponseEntity.ok(stageReadService.getStageTemplate(stageKey));
    }

    @GetMapping("/{stageKey}/assignable-users")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignableUsersForStage(
            @PathVariable String stageKey) {
        var stageConfig = stageReadService.getStageByKey(stageKey);
        List<String> assigneeRoles = stageConfig != null ? stageConfig.getAssigneeRoles() : null;
        if (assigneeRoles == null || assigneeRoles.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userQueryService.getUsersByOfficeAndRoles(assigneeRoles));
    }
}

