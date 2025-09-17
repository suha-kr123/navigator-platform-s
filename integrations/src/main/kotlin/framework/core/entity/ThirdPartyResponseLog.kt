package framework.core.entity

import annotations.NoArg
import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.javers.core.metamodel.annotation.TypeName
import java.util.UUID

@Entity
@Table(name = "third_party_response_log")
@TypeName("third_party_response_log")
@NoArg
@Suppress("LongParameterList")
class ThirdPartyResponseLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "entity_type", length = 3, nullable = false)
    var entityType: Int? = null,

    @Column(name = "entity_id", length = 20, nullable = true)
    var entityId: String? = null,

    @Column(name = "request_method", length = 16, nullable = false)
    var requestMethod: String? = null,

    @Column(name = "url", length = 512, nullable = false)
    var url: String? = null,

    @Column(name = "request")
    var request: String? = null,

    @Column(name = "response")
    var response: String? = null,

    @Column(name = "http_status_code", length = 4)
    var httpStatusCode: Int? = null,

    @Column(name = "response_time_ms", length = 20)
    var responseTimeInMs: Long? = null,

    @Column(name = "provider_name", length = 50)
    var providerName: String? = null,

    @Column(name = "provider_config_id", length = 50)
    var providerRefId: String? = null,

    @Column(name = "business_purpose")
    var businessPurpose: String? = null,

    @Column(name = "business_entity")
    var businessEntityName: String? = null,

    @Column(name = "api_purpose")
    var apiPurpose: String? = null,
) : AuditableEntity()
