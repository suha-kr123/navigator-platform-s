package com.nivasafinance.features.rolemanagement.permission.service

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse
import com.nivasafinance.features.rolemanagement.permission.dto.toPermissionResponse
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionReadRepositoryWrapper
import org.springframework.stereotype.Service

@Service
class PermissionReadServiceImpl(
    private val permissionReadRepositoryWrapper: PermissionReadRepositoryWrapper,
) : PermissionReadService {

    override fun getPermissionsByRoles(roleName: List<String>): List<PermissionResponse> {
        if (roleName.isEmpty()) return emptyList()
        val permissions = permissionReadRepositoryWrapper.findPermissionsByRoleNames(roleName)
        return permissions.mapNotNull { it.toPermissionResponse() }
    }
}
