package com.nivasafinance.features.rolemanagement.permissionchecker

import com.nivasafinance.features.rolemanagement.enums.ActionEnum
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum
import com.nivasafinance.features.rolemanagement.permission.service.PermissionReadService
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService
import org.springframework.stereotype.Service

@Service
class PermissionCheckerServiceImpl(
    private val permissionReadService: PermissionReadService,
    private val userRoleService: UserRoleService
) : PermissionCheckerService {

    override fun checkPermissionForUser(username: String, action: ActionEnum, module: ModuleEnum, operation: OperationsEnum): Boolean {
        val roles = userRoleService.getRolesByUsername(username)
        if (roles.isEmpty()) return false

        val permissions = permissionReadService.getPermissionsByRoles(roles)
        return permissions.any { p -> p.action == action && p.module == module }
    }
}
