package framework.core.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import org.javers.core.metamodel.annotation.Entity as JaversEntity

@Table(name = "f_third_party_service_config")
@Entity
@JaversEntity
data class ThirdPartyServiceConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID,

    @Column(name = "name")
    val name: String,

    @Column(name = "service")
    val service: String,

    @Column(name = "primary_config_key")
    val primaryConfigKey: UUID,

    @Column(name = "fallback_config_key")
    val fallbackConfigKey: UUID? = null,

    @Column(name = "retry_count")
    val retryCount: Int,

    @Column(name = "is_primary")
    val isPrimary: Boolean,

    @Column(name = "is_active")
    val isActive: Boolean,
) : AuditableEntity()
