package com.nivasafinance.features.wrapper.service

import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse

interface ApplicantWrapperWriteService {
    fun createApplicant(request: ApplicantWrapperRequest): ApplicantWrapperResponse
}
