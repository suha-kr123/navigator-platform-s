package com.nivasafinance.features.person.entity

import com.nivasafinance.common.annotations.NoArg
import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.person.enum.Gender
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
import org.javers.core.metamodel.annotation.TypeName
import org.javers.core.metamodel.annotation.Value
import java.time.LocalDate
import java.util.UUID

@Entity
@TypeName("person")
@Table(name = "person")
@NoArg
@Suppress("LongParameterList")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "first_name", length = 100)
    var firstName: String? = null,

    @Column(name = "middle_name", length = 100)
    var middleName: String? = null,

    @Column(name = "last_name", length = 100)
    var lastName: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mobile_numbers", columnDefinition = "jsonb")
    var mobileNumbers: List<MobileNumberDetails>? = null,

    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null,

    @Column(name = "gender", length = 10)
    var gender: Gender? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null

) : AuditableEntity()

@Value
@NoArg
data class MobileNumberDetails(
    var number: String? = null,
    var isPrimary: Boolean? = null,
    var isWhatsappAvailable: Boolean? = null
)
