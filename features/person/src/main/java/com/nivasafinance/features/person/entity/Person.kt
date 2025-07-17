package com.nivasafinance.features.person.entity

import annotations.NoArg
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
import java.io.Serializable

@Entity
@Table(name = "person")
@NoArg
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
) : Serializable {
    @Suppress("MagicNumber")
    val serialVersionUID = 998233332L
}

@NoArg
data class Details(
    val name: String?,
    val phoneNo: String?,
    val whatsappNo: String?,
    val pastPhoneNos: List<String>?,
    val identifiers: List<Identifier>?,
    val addressList: List<HistoryAddress>?
) : Serializable {
    @Suppress("MagicNumber")
    val serialVersionUID = 993282332L
}

@NoArg
data class Identifier(
    val id: Long?,
    val type: String?,
    val frontImageUrl: String?,
    val backImageUrl: String?,
) : Serializable {
    @Suppress("MagicNumber")
    val serialVersionUID = 9982332L
}

@NoArg
data class HistoryAddress(
    val type: String?,
    val addressId: Long?
) : Serializable {
    @Suppress("MagicNumber")
    val serialVersionUID = 88232323L
}
