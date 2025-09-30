package com.nivasafinance.features.lender.lenderoffice.repository

import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import com.nivasafinance.features.lender.lenderoffice.exception.LenderOfficeExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LenderOfficeRepositoryWrapper(
    private val lenderOfficeRepository: LenderOfficeRepository,
    private val messageSource: MessageSource,
) {

    fun saveWithException(lenderOffice: LenderOffice): LenderOffice {
        return try {
            lenderOfficeRepository.save(lenderOffice)
        } catch (ex: Exception) {
            throw LenderOfficeExceptionFactory.createFailed(messageSource)
        }
    }

    fun findByIdWithException(id: UUID): LenderOffice {
        return lenderOfficeRepository.findById(id).orElseThrow {
            LenderOfficeExceptionFactory.lenderOfficeNotFound(id, messageSource)
        }
    }

    fun findByKeyWithException(key: String): LenderOffice {
        return lenderOfficeRepository.findByKey(key)
            ?: throw LenderOfficeExceptionFactory.lenderOfficeKeyNotFound(key, messageSource)
    }

    fun findByLenderKeyAndStatus(
        lenderKey: String,
        status: LenderOfficeStatus
    ): List<LenderOffice> {
        return lenderOfficeRepository.findByLenderKeyAndStatus(lenderKey, status)
    }

    fun deleteByIdWithException(id: UUID) {
        if (!lenderOfficeRepository.existsById(id)) {
            throw LenderOfficeExceptionFactory.lenderOfficeNotFound(id, messageSource)
        }
        lenderOfficeRepository.deleteById(id)
    }
}
