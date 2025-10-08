package com.nivasafinance.features.rolemanagement.permission.repository

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory
import com.nivasafinance.features.rolemanagement.permission.entity.Permission
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PermissionRepositoryWrapper(
    private val permissionRepository: PermissionRepository,
    private val messageSource: MessageSource,
) {

    fun findByIdIn(ids: List<UUID>): List<Permission> {
        return permissionRepository.findByIdIn(ids)
    }

    fun findById(id: UUID): Permission? {
        return permissionRepository.findById(id).orElse(null)
    }

    fun findByIdWithException(id: UUID): Permission {
        return findById(id) ?: throw RoleManagementExceptionFactory.notFound("permission", id, messageSource)
    }
}
