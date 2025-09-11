package com.nivasafinance.features.advisorleadmapping.client

import java.util.UUID

interface AdvisorClient {
    fun getAdvisor(id: UUID): AdvisorInfo
    fun validateAdvisor(id: UUID): Boolean
    fun getAdvisorByMobile(mobileNumber: String): AdvisorInfo?
}

data class AdvisorInfo(
    val id: UUID,
    val advisorCode: String?,
    val status: String,
    val isEmployee: Boolean
)
