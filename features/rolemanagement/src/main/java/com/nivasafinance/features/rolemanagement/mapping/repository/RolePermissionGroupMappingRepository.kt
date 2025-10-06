package com.nivasafinance.features.rolemanagement.mapping.repository

import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RolePermissionGroupMappingRepository : JpaRepository<RolePermissionGroupMapping, UUID>
{
    fun findByRoleIdIn(roleIds: List<UUID>): List<RolePermissionGroupMapping>
}
