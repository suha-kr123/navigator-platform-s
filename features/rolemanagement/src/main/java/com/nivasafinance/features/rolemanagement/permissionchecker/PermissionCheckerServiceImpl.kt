package com.nivasafinance.features.rolemanagement.permissionchecker

import com.nivasafinance.features.rolemanagement.permission.service.PermissionReadService
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService
import com.nivasafinance.features.rolemanagement.mapping.service.UserRoleService
import data.enums.ActionEnum
import data.enums.ModuleEnum
import org.springframework.stereotype.Service

@Service
class PermissionCheckerServiceImpl(
    private val permissionReadService: PermissionReadService,
    private val userRoleService: UserRoleService
) : PermissionCheckerService {

    override fun checkPermissionForUser(username: String, action: ActionEnum, module: ModuleEnum): Boolean {
        val roles = userRoleService.getRolesByUsername(username)
        if (roles.isEmpty()) return false
        
        val permissions = permissionReadService.getPermissionsByRoles(roles)
        return permissions.any { p -> p.action == action && p.module == module }
    }
}
