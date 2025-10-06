package com.nivasafinance.features.rolemanagement.permissiongroup.repository

import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup
import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PermissionGroupRepositoryWrapper(
    private val repository: PermissionGroupRepository,
    private val messageSource: MessageSource,
) {

    fun findAll(): List<PermissionGroup> {
        return repository.findAll()
    }

    fun findById(id: UUID): PermissionGroup? {
        return repository.findById(id).orElse(null)
    }

    fun findByIdWithException(id: UUID): PermissionGroup {
        return findById(id) ?: throw RoleManagementExceptionFactory.notFound("permission.group", id, messageSource)
    }
}
