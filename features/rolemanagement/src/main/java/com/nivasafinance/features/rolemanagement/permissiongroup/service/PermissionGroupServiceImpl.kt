package com.nivasafinance.features.rolemanagement.permissiongroup.service

import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse
import com.nivasafinance.features.rolemanagement.permissiongroup.repository.PermissionGroupRepositoryWrapper
import com.nivasafinance.features.rolemanagement.role.repository.RoleRepositoryWrapper
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PermissionGroupServiceImpl(
    private val permissionGroupRepositoryWrapper: PermissionGroupRepositoryWrapper,
    private val rolePermissionGroupMappingRepositoryWrapper: RolePermissionGroupMappingRepositoryWrapper,
    private val roleRepositoryWrapper: RoleRepositoryWrapper
) : PermissionGroupService {

    override fun getPermissionGroupsbyRoles(roleName: List<String>): List<PermissionGroupResponse> {
        if (roleName.isEmpty()) return emptyList()

        val roles = roleRepositoryWrapper.findByNameIn(roleName)
        if (roles.isEmpty()) return emptyList()

        val roleIds = roles.mapNotNull { it.id }
        val rolePermissionGroupMappings = rolePermissionGroupMappingRepositoryWrapper.findByRoleIdIn(roleIds)
        val permissionGroupIds = rolePermissionGroupMappings.map { it.permissionGroupId }.distinct()

        if (permissionGroupIds.isEmpty()) return emptyList()

        val permissionGroups = permissionGroupIds.mapNotNull { id ->
            permissionGroupRepositoryWrapper.findById(id)
        }

        return permissionGroups.map { group ->
            PermissionGroupResponse(
                id = group.id ?: UUID.randomUUID(),
                name = group.name
            )
        }
    }
}
