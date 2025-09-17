package framework.core.repository

import framework.core.entity.ThirdPartyResponseLog
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThirdPartyResponseLogRepository : JpaRepository<ThirdPartyResponseLog, UUID>
