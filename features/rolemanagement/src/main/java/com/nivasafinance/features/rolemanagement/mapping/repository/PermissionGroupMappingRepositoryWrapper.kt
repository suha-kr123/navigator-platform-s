package com.nivasafinance.features.rolemanagement.mapping.repository

import com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping
import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PermissionGroupMappingRepositoryWrapper(
    private val repository: PermissionGroupMappingRepository,
    private val messageSource: MessageSource,
) {

    fun findByPermissionGroupIdIn(groupIds: List<UUID>): List<PermissionGroupMapping> {
        return repository.findByPermissionGroupIdIn(groupIds)
    }

    fun findById(id: UUID): PermissionGroupMapping? {
        return repository.findById(id).orElse(null)
    }

    fun findByIdWithException(id: UUID): PermissionGroupMapping {
        return findById(id) ?: throw RoleManagementExceptionFactory.notFound("permission.group.mapping", id, messageSource)
    }
}


