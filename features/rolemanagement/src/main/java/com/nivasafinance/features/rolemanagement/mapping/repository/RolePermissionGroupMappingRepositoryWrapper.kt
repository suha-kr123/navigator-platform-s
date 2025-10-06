package com.nivasafinance.features.rolemanagement.mapping.repository

import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping
import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RolePermissionGroupMappingRepositoryWrapper(
    private val repository: RolePermissionGroupMappingRepository,
    private val messageSource: MessageSource,
) {

    fun findByRoleIdIn(roleIds: List<UUID>): List<RolePermissionGroupMapping> {
        return repository.findByRoleIdIn(roleIds)
    }

    fun findById(id: UUID): RolePermissionGroupMapping? {
        return repository.findById(id).orElse(null)
    }

    fun findByIdWithException(id: UUID): RolePermissionGroupMapping {
        return findById(id) ?: throw RoleManagementExceptionFactory.notFound("role.permission.group.mapping", id, messageSource)
    }
}


