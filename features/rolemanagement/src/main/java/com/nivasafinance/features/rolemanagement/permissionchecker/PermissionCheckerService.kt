package com.nivasafinance.features.rolemanagement.permissionchecker

import com.nivasafinance.features.rolemanagement.enums.ActionEnum
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum

interface PermissionCheckerService {
    fun checkPermissionForUser(
        username: String,
        action: ActionEnum,
        module: ModuleEnum,
        operation: OperationsEnum
    ): Boolean
}
