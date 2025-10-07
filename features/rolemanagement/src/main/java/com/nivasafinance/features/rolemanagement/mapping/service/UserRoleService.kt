package com.nivasafinance.features.rolemanagement.mapping.service

import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository
import org.springframework.stereotype.Service

@Service
class UserRoleService(
    private val userRoleMappingRepository: UserRoleMappingRepository
) {
    fun getRolesByUsername(username: String): List<String> {
        return userRoleMappingRepository.findByUsername(username)
            .map { listOf(it.role) }
            .orElse(emptyList())
    }
}
