package framework.core.repository

import framework.core.entity.ThirdPartyProviderConfig
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThirdPartyProviderConfigRepository : JpaRepository<ThirdPartyProviderConfig, UUID>
