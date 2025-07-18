package com.nivasafinance.features.person.entity

import annotations.NoArg
import data.Identifier
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "person")
@NoArg
@Suppress("LongParameterList")
class Person(
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "first_name", length = 100)
    val firstName: String?,

    @Column(name = "middle_name", length = 100)
    val middleName: String?,

    @Column(name = "last_name", length = 100)
    val lastName: String?,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mobile_number", columnDefinition = "jsonb")
    val mobileNumber: MobileNumberDetails?,

    @Column(name = "email", length = 100)
    val email: String?,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate?,

    @Column(name = "gender", length = 10)
    val gender: String?,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "identifiers", columnDefinition = "jsonb")
    val identifiers: List<Identifier>,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addresses", columnDefinition = "jsonb")
    val addresses: List<AddressDetails>,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val dataExt: Details?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime?,

    @Column(name = "created_by", length = 100)
    val createdBy: String?,

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime?,

    @Column(name = "updated_by", length = 100)
    val updatedBy: String?
)

@NoArg
data class MobileNumberDetails(
    val primary: String,
)

@NoArg
data class AddressDetails(
    val addressId: String,
    val type: String,
)

@NoArg
data class Details(
    val description: String,
)
