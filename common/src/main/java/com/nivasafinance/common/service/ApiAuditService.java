package com.nivasafinance.common.service;

import com.nivasafinance.common.audit.ApiAuditLog;
import com.nivasafinance.common.repository.ApiAuditLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ApiAuditService {
    private final ApiAuditLogRepository apiAuditLogRepository;

    public ApiAuditService(ApiAuditLogRepository apiAuditLogRepository) {
        this.apiAuditLogRepository = apiAuditLogRepository;
    }

    @Transactional
    public void saveAuditLog(ApiAuditLog log) {
        apiAuditLogRepository.save(log);
    }
}

