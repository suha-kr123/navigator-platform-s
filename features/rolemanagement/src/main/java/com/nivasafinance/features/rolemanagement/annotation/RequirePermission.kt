package com.nivasafinance.features.rolemanagement.annotation

import com.nivasafinance.features.rolemanagement.enums.ActionEnum
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequirePermission(
    val action: ActionEnum,
    val module: ModuleEnum,
    val operation: OperationsEnum
)
