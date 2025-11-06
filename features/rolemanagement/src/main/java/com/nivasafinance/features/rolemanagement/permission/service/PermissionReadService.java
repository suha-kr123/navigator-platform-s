package com.nivasafinance.features.rolemanagement.permission.service;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;

import java.util.List;

public interface PermissionReadService {
    List<PermissionResponse> getPermissionsByRoles(List<String> roleName);
}

