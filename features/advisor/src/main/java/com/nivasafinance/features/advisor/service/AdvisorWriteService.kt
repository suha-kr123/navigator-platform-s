package com.nivasafinance.features.advisor.service

import com.nivasafinance.features.advisor.dto.AdvisorDto
import java.util.UUID

interface AdvisorWriteService {

    fun createAdvisor(advisorDto: AdvisorDto): AdvisorDto
    fun deleteAdvisor(id: UUID): AdvisorDto
    fun updateAdvisor(id: UUID, advisorDto: AdvisorDto): AdvisorDto
}