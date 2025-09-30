package com.nivasafinance.features.lender.lender.service

import com.nivasafinance.features.lender.lender.dto.LenderResponseData
import com.nivasafinance.features.lender.lender.enum.LenderStatus
import java.util.UUID

interface LenderReadService {
    fun getById(id: UUID): LenderResponseData
    fun getByKey(key: String): LenderResponseData
    fun getAllByStatus(status: LenderStatus): List<LenderResponseData>
}
