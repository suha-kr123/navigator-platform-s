package com.nivasafinance.features.lender.lenderoffice.service

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import java.util.UUID

interface LenderOfficeReadService {
    fun getByKey(key: String): LenderOfficeReponseData
    fun getById(id: UUID): LenderOfficeReponseData
    fun getByLenderKeyAndStatus(lenderKey: String, status: LenderOfficeStatus): List<LenderOfficeReponseData>
}
