package framework.core.repository

import framework.core.entity.ThirdPartyServiceConfig
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThirdPartyServiceConfigRepository : JpaRepository<ThirdPartyServiceConfig, UUID> {
    fun findByServiceAndIsActiveTrue(serviceName: String): ThirdPartyServiceConfig?
}
