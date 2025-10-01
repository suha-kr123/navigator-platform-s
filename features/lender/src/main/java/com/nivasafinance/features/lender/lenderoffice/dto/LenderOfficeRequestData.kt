package com.nivasafinance.features.lender.lenderoffice.dto

import com.nivasafinance.features.address.dto.CreateAddressRequest
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus

data class LenderOfficeRequestData(
    val name: String,
    val key: String,
    val lenderKey: String,
    val createAddressRequest: CreateAddressRequest,
    val status: LenderOfficeStatus
)
