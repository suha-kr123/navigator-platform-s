package com.nivasafinance.features.rolemanagement.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionBatchCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckRequest;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckResponse;
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.rolemanagement.enums.Role;
import com.nivasafinance.common.context.UserContext;


import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/permissions")
@RequiredArgsConstructor
public class PermissionCheckController {

    private final PermissionCheckerService permissionCheckerService;
    private final UserRoleService userRoleService;

    @GetMapping("/check")
    public ResponseEntity<PermissionCheckResponse> checkPermission(
            @RequestParam("permission") String permissionName) {
        PermissionCheckResponse response = permissionCheckerService.checkPermission(permissionName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-batch")
    public ResponseEntity<PermissionBatchCheckResponse> checkPermissionsBatch(
            @Valid @RequestBody PermissionCheckRequest request) {
        PermissionBatchCheckResponse response = permissionCheckerService.checkPermissionsBatch(
                request.getPermissionNames());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        List<String> roles = userRoleService.getRolesByUsername(UserContext.getUsername());
        return ResponseEntity.ok(roles);
    }
}
