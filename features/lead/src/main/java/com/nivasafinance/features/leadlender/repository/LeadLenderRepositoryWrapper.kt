package com.nivasafinance.features.leadlender.repository

import com.nivasafinance.features.leadlender.entity.LeadLender
import com.nivasafinance.features.leadlender.enum.LeadLenderStatus
import com.nivasafinance.features.leadlender.exception.LeadLenderExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadLenderRepositoryWrapper(
    private val leadLenderRepository: LeadLenderRepository,
    private val messageSource: MessageSource,
) {

    fun saveWithException(leadLender: LeadLender): LeadLender {
        return try {
            leadLenderRepository.save(leadLender)
        } catch (ex: Exception) {
            throw LeadLenderExceptionFactory.createFailed(messageSource)
        }
    }

    fun findByIdWithException(id: UUID): LeadLender {
        return leadLenderRepository.findById(id).orElseThrow {
            LeadLenderExceptionFactory.leadLenderNotFound(id, messageSource)
        }
    }

    fun findByLeadId(leadId: UUID): List<LeadLender> {
        return leadLenderRepository.findByLeadId(leadId)
    }

    fun findByLenderKey(lenderKey: String): List<LeadLender> {
        return leadLenderRepository.findByLenderKey(lenderKey)
    }

    fun findByStatus(status: LeadLenderStatus): List<LeadLender> {
        return leadLenderRepository.findByStatus(status)
    }

    fun findByLeadIdAndStatus(leadId: UUID, status: LeadLenderStatus): List<LeadLender> {
        return leadLenderRepository.findByLeadIdAndStatus(leadId, status)
    }

    fun findByLeadIdAndStatusOrderByCreatedAtDesc(
        leadId: UUID,
        status: LeadLenderStatus
    ): List<LeadLender> {
        return leadLenderRepository.findByLeadIdAndStatusOrderByCreatedAtDesc(leadId, status)
    }

    fun findByLeadIdAndLenderKey(leadId: UUID, lenderKey: String): LeadLender? {
        return leadLenderRepository.findByLeadIdAndLenderKey(leadId, lenderKey)
    }

    fun findByLeadIdAndLenderKeyWithException(leadId: UUID, lenderKey: String): LeadLender {
        return findByLeadIdAndLenderKey(leadId, lenderKey)
            ?: throw LeadLenderExceptionFactory.leadLenderNotFound(leadId, lenderKey, messageSource)
    }

    fun deleteByIdWithException(id: UUID) {
        if (!leadLenderRepository.existsById(id)) {
            throw LeadLenderExceptionFactory.leadLenderNotFound(id, messageSource)
        }
        leadLenderRepository.deleteById(id)
    }
}
