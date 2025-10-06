package com.nivasafinance.features.rolemanagement.permissiongroup.service

import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse
import java.util.UUID

interface PermissionGroupService {
    fun getPermissionGroupsbyRoles(roleName: List<String>): List<PermissionGroupResponse>
}
