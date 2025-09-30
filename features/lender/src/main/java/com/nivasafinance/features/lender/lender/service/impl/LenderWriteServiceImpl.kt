package com.nivasafinance.features.lender.lender.service.impl

import com.nivasafinance.features.lender.lender.dto.LenderRequestData
import com.nivasafinance.features.lender.lender.dto.LenderResponseData
import com.nivasafinance.features.lender.lender.entity.Lender
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper
import com.nivasafinance.features.lender.lender.service.LenderWriteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LenderWriteServiceImpl(
    private val lenderRepositoryWrapper: LenderRepositoryWrapper
) : LenderWriteService {

    override fun create(lenderData: LenderRequestData): LenderResponseData {
        val lender = Lender(
            key = lenderData.key,
            name = lenderData.name,
            status = lenderData.status
        )
        val savedLender = lenderRepositoryWrapper.saveWithException(lender)
        return savedLender.toResponse()
    }

    override fun update(id: UUID, lenderData: LenderRequestData): LenderResponseData {
        val existingLender = lenderRepositoryWrapper.findByIdWithException(id)

        val updatedLender = existingLender.copy(
            key = lenderData.key,
            name = lenderData.name,
            status = lenderData.status
        )
        val savedLender = lenderRepositoryWrapper.saveWithException(updatedLender)
        return savedLender.toResponse()
    }

    override fun delete(id: UUID) {
        lenderRepositoryWrapper.deleteByIdWithException(id)
    }

    private fun Lender.toResponse(): LenderResponseData {
        return LenderResponseData(
            id = checkNotNull(this.id) { "Lender ID cannot be null" },
            key = this.key,
            name = this.name,
            status = this.status
        )
    }
}
