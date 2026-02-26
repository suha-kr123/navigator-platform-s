package com.nivasafinance.features.rolemanagement.admin.service;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupWithPermissionsResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.UpdateRolePermissionGroupsRequest;
import com.nivasafinance.features.rolemanagement.permission.dto.UpdateRolePermissionsRequest;
import com.nivasafinance.features.rolemanagement.role.dto.CreateRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.RoleResponse;

import java.util.List;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionsUpdateResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionGroupsUpdateResponse;

public interface AdminRoleManagementService {
    List<PermissionResponse> getAllPermissions();
    PaginatedResponse<PermissionResponse> getPermissions(PaginationRequest pagination, String q);
    List<PermissionGroupResponse> getAllPermissionGroups();
    List<PermissionGroupWithPermissionsResponse> getAllPermissionGroupsWithPermissions();
    List<RoleResponse> getAllRoles();
    PaginatedResponse<RoleResponse> getRoles(PaginationRequest pagination, String q);
    PaginatedResponse<PermissionGroupResponse> getPermissionGroups(PaginationRequest pagination, String q);
    List<PermissionResponse> getRolePermissions(String role);
    List<PermissionGroupResponse> getRolePermissionGroups(String role);
    RoleResponse createRole(CreateRoleRequest request);
    RolePermissionsUpdateResponse addPermissionsToRole(String role, UpdateRolePermissionsRequest request);
    void removePermissionFromRole(String role, Long permissionId);
    RolePermissionGroupsUpdateResponse addPermissionGroupsToRole(String role, UpdateRolePermissionGroupsRequest request);
    void removePermissionGroupFromRole(String role, Long groupId);
}
