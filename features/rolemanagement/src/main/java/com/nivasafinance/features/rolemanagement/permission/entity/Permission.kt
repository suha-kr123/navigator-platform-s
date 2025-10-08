package com.nivasafinance.features.rolemanagement.permission.entity

import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.rolemanagement.enums.ActionEnum
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    @Enumerated(EnumType.STRING)
    val action: ActionEnum? = null,

    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    val operation: OperationsEnum? = null,

    @Column(name = "module")
    @Enumerated(EnumType.STRING)
    val module: ModuleEnum? = null
) : AuditableEntity()
