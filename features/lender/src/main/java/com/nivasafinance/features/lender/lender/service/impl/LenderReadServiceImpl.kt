package com.nivasafinance.features.lender.lender.service.impl

import com.nivasafinance.features.lender.lender.dto.LenderResponseData
import com.nivasafinance.features.lender.lender.entity.Lender
import com.nivasafinance.features.lender.lender.enum.LenderStatus
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper
import com.nivasafinance.features.lender.lender.service.LenderReadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class LenderReadServiceImpl(
    private val lenderRepositoryWrapper: LenderRepositoryWrapper
) : LenderReadService {

    override fun getById(id: UUID): LenderResponseData {
        return lenderRepositoryWrapper.findByIdWithException(id).toResponse()
    }

    override fun getByKey(key: String): LenderResponseData {
        val lender = lenderRepositoryWrapper.findByKeyWithException(key)
        return lender.toResponse()
    }

    override fun getAllByStatus(status: LenderStatus): List<LenderResponseData> {
        return lenderRepositoryWrapper.findAllByStatus(status).map { it.toResponse() }
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
