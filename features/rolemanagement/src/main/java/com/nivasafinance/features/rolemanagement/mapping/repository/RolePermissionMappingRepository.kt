package com.nivasafinance.features.rolemanagement.mapping.repository

import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RolePermissionMappingRepository : JpaRepository<RolePermissionMapping, UUID>
{
    fun findByRoleIdIn(roleIds: List<UUID>): List<RolePermissionMapping>
}
