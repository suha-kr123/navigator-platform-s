package com.nivasafinance.features.advisor.dto

import annotations.NoArg
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.dto.PersonDto
import java.util.UUID

@NoArg
data class AdvisorDto(
    val id: UUID? = null,
    val advisorCode: String? = null,
    val status: AdvisorStatus? = null,
    var personalDetails: PersonDto = PersonDto()
)
