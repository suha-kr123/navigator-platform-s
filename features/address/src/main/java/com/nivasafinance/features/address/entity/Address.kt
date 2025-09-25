package com.nivasafinance.features.address.entity

import audit.AuditableEntity
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "address")
data class Address(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(name = "address_type", nullable = false)
    var addressType: String? = null,

    @Column(name = "is_primary", nullable = false)
    var isPrimary: Boolean = false,

    @Column(name = "address_one")
    var addressOne: String? = null,

    @Column(name = "address_two")
    var addressTwo: String? = null,

    @Column(name = "landmark")
    var landmark: String? = null,

    @Column(name = "district")
    var district: String? = null,

    @Column(name = "state")
    var state: String? = null,

    @Column(name = "pincode")
    var pincode: String,

    @Column(name = "address_source")
    var addressSource: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null,
) : AuditableEntity()
