package com.nivasafinance.features.rolemanagement.permission.repository

import com.nivasafinance.features.rolemanagement.permission.entity.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByIdIn(ids: List<UUID>): List<Permission>
}
