package com.nivasafinance.features.wrapper.dto

data class CreateLeadForAdvisorRequest(
    val applicantWrapperRequest: ApplicantWrapperRequest,
    val remarks: String? = null
)
