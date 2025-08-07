package service

import audit.ApiAuditLog
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import repository.ApiAuditLogRepository

@Service
class ApiAuditService(
    private val apiAuditLogRepository: ApiAuditLogRepository
) {
    @Transactional
    fun saveAuditLog(log: ApiAuditLog) {
        apiAuditLogRepository.save(log)
    }
}
