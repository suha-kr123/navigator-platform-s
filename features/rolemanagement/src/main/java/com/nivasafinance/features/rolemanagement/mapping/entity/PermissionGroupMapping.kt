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
@Table(name = "permission_group_mapping")
class PermissionGroupMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(name = "permission_id", nullable = false)
    val permissionId: UUID,

    @Column(name = "permission_group_id", nullable = false)
    val permissionGroupId: UUID
) : AuditableEntity()
