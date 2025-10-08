package com.nivasafinance.features.rolemanagement.mapping.repository

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RolePermissionMappingRepositoryWrapper(
    private val repository: RolePermissionMappingRepository,
    private val messageSource: MessageSource,
) {

    fun findByRoleIdIn(roleIds: List<UUID>): List<RolePermissionMapping> {
        return repository.findByRoleIdIn(roleIds)
    }

    fun findById(id: UUID): RolePermissionMapping? {
        return repository.findById(id).orElse(null)
    }

    fun findByIdWithException(id: UUID): RolePermissionMapping {
        return findById(id) ?: throw RoleManagementExceptionFactory.notFound("role.permission.mapping", id, messageSource)
    }
}
