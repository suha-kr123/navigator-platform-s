package com.nivasafinance.features.rolemanagement.permissionchecker

import data.enums.ActionEnum
import data.enums.ModuleEnum

interface PermissionCheckerService {
    fun checkPermissionForUser(username : String, action: ActionEnum, module: ModuleEnum): Boolean
}


