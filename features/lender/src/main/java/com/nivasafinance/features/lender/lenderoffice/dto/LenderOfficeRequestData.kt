package com.nivasafinance.features.lender.lenderoffice.dto

import com.nivasafinance.common.dto.AddressData
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus

data class LenderOfficeRequestData(
    val name: String,
    val key: String,
    val lenderKey: String,
    val createAddressRequest: AddressData?,
    val status: LenderOfficeStatus
)
