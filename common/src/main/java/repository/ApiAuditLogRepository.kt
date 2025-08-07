package repository

import audit.ApiAuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApiAuditLogRepository : JpaRepository<ApiAuditLog, Long>
