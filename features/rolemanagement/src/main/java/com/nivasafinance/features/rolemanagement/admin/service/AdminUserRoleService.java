package com.nivasafinance.features.rolemanagement.admin.service;

import com.nivasafinance.features.rolemanagement.role.dto.AddUserRolesRequest;
import com.nivasafinance.features.rolemanagement.role.dto.SetPrimaryRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.UserRolesResponse;

public interface AdminUserRoleService {
    UserRolesResponse getUserRoles(String username);
    UserRolesResponse addUserRoles(String username, AddUserRolesRequest request);
    void removeUserRole(String username, String role);
    UserRolesResponse setPrimaryRole(String username, SetPrimaryRoleRequest request);
}
