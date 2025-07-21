package com.nivasafinance.features.person.entity

import annotations.NoArg
import data.Identifier
import data.enums.Gender
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.Version
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "person")
@NoArg
@Suppress("LongParameterList")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,

    @Column(name = "first_name", length = 100)
    val firstName: String? = null,

    @Column(name = "middle_name", length = 100)
    val middleName: String? = null,

    @Column(name = "last_name", length = 100)
    val lastName: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mobile_number", columnDefinition = "jsonb")
    val mobileNumber: MobileNumberDetails? = null,

    @Column(name = "email", length = 100)
    val email: String? = null,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    val gender: Gender? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "identifiers", columnDefinition = "jsonb")
    val identifiers: List<Identifier>? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addresses", columnDefinition = "jsonb")
    val addresses: List<AddressDetails>? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val dataExt: Details? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null, // todo use extend class

    @Column(name = "created_by", length = 100)
    val createdBy: String? = null,

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "updated_by", length = 100)
    val updatedBy: String? = null,

    @Version
    private val version: Long = 0
)

@NoArg
data class MobileNumberDetails(
    val primary: String? = null,
)

@NoArg
data class AddressDetails(
    val addressId: String? = null,
    val type: String? = null,
)

@NoArg
data class Details(
    val description: String? = null,
)
