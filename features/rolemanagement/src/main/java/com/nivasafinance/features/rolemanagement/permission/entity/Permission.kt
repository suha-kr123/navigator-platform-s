package com.nivasafinance.features.rolemanagement.permission.entity

import com.nivasafinance.common.audit.AuditableEntity
import data.enums.ActionEnum
import data.enums.ModuleEnum
import data.enums.OperationsEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "permissions")
class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(name = "name", nullable = false, unique = true)
    val name: String,

    @Column(name = "action")
    val action: ActionEnum? = null,

    @Column(name = "operation")
    val operation: OperationsEnum? = null,

    @Column(name = "module")
    val module: ModuleEnum? = null
) : AuditableEntity()
