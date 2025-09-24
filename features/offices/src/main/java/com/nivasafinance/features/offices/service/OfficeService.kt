package com.nivasafinance.features.offices.service

import com.nivasafinance.features.offices.dto.OfficeResponse
import java.util.UUID

interface OfficeService {
    fun getOffice(id: UUID): OfficeResponse
}
