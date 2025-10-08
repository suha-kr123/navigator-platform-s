package com.nivasafinance.features.rolemanagement.permission.repository

import com.nivasafinance.features.rolemanagement.mapping.repository.PermissionGroupMappingRepositoryWrapper
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionMappingRepositoryWrapper
import com.nivasafinance.features.rolemanagement.permission.entity.Permission
import com.nivasafinance.features.rolemanagement.role.repository.RoleRepositoryWrapper
import org.springframework.stereotype.Service

@Service
class PermissionReadRepositoryWrapper(
    private val roleRepository: RoleRepositoryWrapper,
    private val rolePermissionMappingRepository: RolePermissionMappingRepositoryWrapper,
    private val rolePermissionGroupMappingRepository: RolePermissionGroupMappingRepositoryWrapper,
    private val permissionGroupMappingRepository: PermissionGroupMappingRepositoryWrapper,
    private val permissionRepository: PermissionRepositoryWrapper
) {

    fun findPermissionsByRoleNames(roleNames: List<String>): List<Permission> {
        if (roleNames.isEmpty()) return emptyList()

        val roles = roleRepository.findByNameIn(roleNames)
        if (roles.isEmpty()) return emptyList()

        val roleIds = roles.mapNotNull { it.id }

        val directPermissionIds = rolePermissionMappingRepository
            .findByRoleIdIn(roleIds)
            .map { it.permissionId }

        val groupIds = rolePermissionGroupMappingRepository
            .findByRoleIdIn(roleIds)
            .map { it.permissionGroupId }

        val groupPermissionIds = if (groupIds.isEmpty()) {
            emptyList()
        } else {
            permissionGroupMappingRepository
                .findByPermissionGroupIdIn(groupIds)
                .map { it.permissionId }
        }

        val allPermissionIds = (directPermissionIds + groupPermissionIds)
            .toSet()
            .toList()

        if (allPermissionIds.isEmpty()) return emptyList()

        return permissionRepository.findByIdIn(allPermissionIds)
    }
}
