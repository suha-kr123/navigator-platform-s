package com.nivasafinance.features.lender.lender.service

import com.nivasafinance.features.lender.lender.dto.LenderRequestData
import com.nivasafinance.features.lender.lender.dto.LenderResponseData
import java.util.UUID

interface LenderWriteService {
    fun create(lenderData: LenderRequestData): LenderResponseData
    fun update(id: UUID, lenderData: LenderRequestData): LenderResponseData
    fun delete(id: UUID)
}
