package com.nivasafinance.features.lender.lenderoffice.dto

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus

data class LenderOfficeRequestData(
    val name: String,
    val key: String,
    val lenderKey: String,
    val addressCreateRequest: AddressCreateRequest,
    val status: LenderOfficeStatus
)
