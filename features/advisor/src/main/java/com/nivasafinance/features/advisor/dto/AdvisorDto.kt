package com.nivasafinance.features.advisor.dto

import annotations.NoArg
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.dto.PersonDto
import jakarta.validation.Valid
import java.util.UUID

@NoArg
data class AdvisorDto(
    val id: UUID? = null,
    val advisorCode: String? = null,
    val status: AdvisorStatus? = null,
    @field:Valid
    var personalDetails: PersonDto = PersonDto()
)
