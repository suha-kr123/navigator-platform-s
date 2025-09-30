package com.nivasafinance.features.lender.lender.dto

import com.nivasafinance.features.lender.lender.enum.LenderStatus
import java.util.UUID

data class LenderResponseData(
    val id: UUID,
    val name: String,
    val key: String,
    val status: LenderStatus
)
