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

@Table(name = "third_party_provider_config")
@Entity
@JaversEntity
data class ThirdPartyProviderConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,

    @Column(name = "name")
    val name: String,

    @Column(name = "provider")
    val provider: String,

    @Column(name = "configs")
    val configs: String,

    @Column(name = "active")
    val active: Boolean,

) : AuditableEntity()
