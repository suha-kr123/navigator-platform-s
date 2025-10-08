package com.nivasafinance.features.rolemanagement.mapping.repository

import com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PermissionGroupMappingRepository : JpaRepository<PermissionGroupMapping, UUID> {
    fun findByPermissionGroupIdIn(groupIds: List<UUID>): List<PermissionGroupMapping>
}
