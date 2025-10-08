package com.nivasafinance.features.rolemanagement.permissiongroup.service

import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse

interface PermissionGroupService {
    fun getPermissionGroupsbyRoles(roleName: List<String>): List<PermissionGroupResponse>
}
