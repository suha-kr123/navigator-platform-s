package com.nivasafinance.features.rolemanagement.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionBatchCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckRequest;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.service.PermissionCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/permissions")
@RequiredArgsConstructor
public class PermissionCheckController {

    private final PermissionCheckService permissionCheckService;

    @GetMapping("/check")
    public ResponseEntity<PermissionCheckResponse> checkPermission(
            @RequestParam("permission") String permissionName) {
        PermissionCheckResponse response = permissionCheckService.checkPermission(permissionName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-batch")
    public ResponseEntity<PermissionBatchCheckResponse> checkPermissionsBatch(
            @Valid @RequestBody PermissionCheckRequest request) {
        PermissionBatchCheckResponse response = permissionCheckService.checkPermissionsBatch(
                request.getPermissionNames());
        return ResponseEntity.ok(response);
    }
}
