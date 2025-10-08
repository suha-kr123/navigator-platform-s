package com.nivasafinance.features.rolemanagement.role.repository

import com.nivasafinance.features.rolemanagement.role.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByNameIn(names: List<String>): List<Role>
}
