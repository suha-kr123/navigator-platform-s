package com.nivasafinance.features.offices.service

import com.nivasafinance.features.offices.dto.OfficeCreateRequest
import com.nivasafinance.features.offices.dto.OfficeResponse

interface OfficeWriteService {
    fun createOffice(request: OfficeCreateRequest): OfficeResponse
}
