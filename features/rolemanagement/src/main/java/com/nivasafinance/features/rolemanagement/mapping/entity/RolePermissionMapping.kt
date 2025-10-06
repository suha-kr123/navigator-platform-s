package com.nivasafinance.features.rolemanagement.mapping.entity

import com.nivasafinance.common.audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "role_permission_mapping")
class RolePermissionMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(name = "role_id", nullable = false)
    val roleId: UUID,

    @Column(name = "permission_id", nullable = false)
    val permissionId: UUID
) : AuditableEntity()
