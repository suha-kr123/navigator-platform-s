package com.nivasafinance.features.lender.lenderoffice.service

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData
import java.util.UUID

interface LenderOfficeWriteService {
    fun create(lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData
    fun update(id: UUID, lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData
    fun delete(id: UUID)
}
