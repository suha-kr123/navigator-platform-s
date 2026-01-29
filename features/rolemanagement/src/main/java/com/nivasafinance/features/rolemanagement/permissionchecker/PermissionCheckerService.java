package com.nivasafinance.features.rolemanagement.permissionchecker;

import java.util.List;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionBatchCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckResponse;

public interface PermissionCheckerService {
    boolean checkPermissionForUser(
            String username,
            String permissionName
    );

    PermissionCheckResponse checkPermission(String permissionName);

    PermissionBatchCheckResponse checkPermissionsBatch(List<String> permissionNames);
}

