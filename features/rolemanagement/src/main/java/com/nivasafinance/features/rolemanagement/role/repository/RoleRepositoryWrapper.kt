package com.nivasafinance.features.rolemanagement.role.repository

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory
import com.nivasafinance.features.rolemanagement.role.entity.Role
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RoleRepositoryWrapper(
    private val roleRepository: RoleRepository,
    private val messageSource: MessageSource,
) {

    fun findByNameIn(names: List<String>): List<Role> {
        return roleRepository.findByNameIn(names)
    }

    fun findById(id: UUID): Role? {
        return roleRepository.findById(id).orElse(null)
    }

    fun findByIdWithException(id: UUID): Role {
        return findById(id) ?: throw RoleManagementExceptionFactory.notFound("role", id, messageSource)
    }
}