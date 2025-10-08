package com.nivasafinance.features.rolemanagement.permission.dto

import com.nivasafinance.features.rolemanagement.enums.ActionEnum
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum
import com.nivasafinance.features.rolemanagement.permission.entity.Permission
import java.util.UUID

data class PermissionResponse(
    val id: UUID,
    val name: String,
    val action: ActionEnum?,
    val operation: OperationsEnum?,
    val module: ModuleEnum?
)

fun Permission.toPermissionResponse(): PermissionResponse? {
    val safeId = id ?: return null
    return PermissionResponse(
        id = safeId,
        name = name,
        action = action,
        operation = operation,
        module = module
    )
}
