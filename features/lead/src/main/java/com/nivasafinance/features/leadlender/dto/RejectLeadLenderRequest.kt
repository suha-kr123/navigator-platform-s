package com.nivasafinance.features.leadlender.dto

import com.nivasafinance.features.leadlender.enum.RejectReason
import jakarta.validation.constraints.NotNull

data class RejectLeadLenderRequest(
    @field:NotNull(message = "Reject reason is required")
    val rejectReason: RejectReason
)
