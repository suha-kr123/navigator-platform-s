package com.nivasafinance.features.lender.lender.dto

import com.nivasafinance.features.lender.lender.enum.LenderStatus

data class LenderRequestData(
    val key: String,
    val name: String,
    val status: LenderStatus
)
