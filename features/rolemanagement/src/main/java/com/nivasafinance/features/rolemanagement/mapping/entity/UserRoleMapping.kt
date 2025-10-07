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
@Table(name = "user_role_mapping")
class UserRoleMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(name = "username", nullable = false, unique = true)
    val username: String,

    @Column(name = "role", nullable = false)
    val role: String
) : AuditableEntity()
