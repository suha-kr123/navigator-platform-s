package com.nivasafinance.common.service

import com.nivasafinance.common.audit.ApiAuditLog
import com.nivasafinance.common.repository.ApiAuditLogRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ApiAuditService(
    private val apiAuditLogRepository: ApiAuditLogRepository
) {
    @Transactional
    fun saveAuditLog(log: ApiAuditLog) {
        apiAuditLogRepository.save(log)
    }
}
