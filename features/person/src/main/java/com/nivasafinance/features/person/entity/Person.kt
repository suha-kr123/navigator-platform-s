package com.nivasafinance.features.person.entity

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.io.Serializable

@Entity
@Table(name = "person")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,
    @Column(name = "profile_id")
    val profileId: String,
    @Type(JsonType::class) // tells Hibernate to use Hypersistence’s JSON type handler
    @JdbcTypeCode(SqlTypes.JSON) // ensures Hibernate treats the column as JSON/JSONB
    @Column(name = "details", columnDefinition = "jsonb") // ensures the correct column type in PostgreSQL
    val details: Details?
)

data class Details(
    val name: String?,
    val phoneNo: String?,
    val whatsappNo: String?,
    val pastPhoneNos: List<String>?,
    val identifiers: List<Identifier>?,
    val addressList: List<HistoryAddress>?
) : Serializable

data class Identifier(
    val id: Long?,
    val type: String?,
    val frontImageUrl: String?,
    val backImageUrl: String?,
) : Serializable

data class HistoryAddress(
    val type: String?,
    val addressId: Long?
) : Serializable