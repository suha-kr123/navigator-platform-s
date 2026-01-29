package com.nivasafinance.features.rolemanagement.permission.service;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionBatchCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckResponse;

import java.util.List;

public interface PermissionCheckService {

    PermissionCheckResponse checkPermission(String permissionName);

    PermissionBatchCheckResponse checkPermissionsBatch(List<String> permissionNames);
}
