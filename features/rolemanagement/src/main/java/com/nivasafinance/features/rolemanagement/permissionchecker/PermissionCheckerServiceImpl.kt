package com.nivasafinance.features.rolemanagement.permissionchecker

import com.nivasafinance.features.rolemanagement.permission.service.PermissionReadService
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService
import data.enums.ActionEnum
import data.enums.ModuleEnum
import base.context.UserContext
import org.springframework.stereotype.Service

@Service
class PermissionCheckerServiceImpl(
    private val permissionReadService: PermissionReadService
) : PermissionCheckerService {

    override fun checkPermissionForUser(username: String, action: ActionEnum, module: ModuleEnum): Boolean {
        val userInfo = UserContext.getUserInfo() ?: return false
        val rolesFromContext = userInfo.roles
        if (rolesFromContext.isNotEmpty()) {
            val permissions = permissionReadService.getPermissionsByRoles(rolesFromContext)
            return permissions.any { p -> p.action == action && p.module == module }
        }

        return false
    }
}
