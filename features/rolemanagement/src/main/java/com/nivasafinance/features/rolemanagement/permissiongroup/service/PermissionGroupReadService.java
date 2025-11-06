package com.nivasafinance.features.rolemanagement.permissiongroup.service;

import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;

import java.util.List;

public interface PermissionGroupReadService {
    List<PermissionGroupResponse> getPermissionGroupsbyRoles(List<String> roleName);
}

