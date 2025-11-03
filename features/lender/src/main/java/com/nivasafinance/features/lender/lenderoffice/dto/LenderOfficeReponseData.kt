package com.nivasafinance.features.lender.lenderoffice.dto

import com.nivasafinance.common.dto.AddressData
import java.util.*

data class LenderOfficeReponseData(
    val id: UUID,
    val name: String,
    val key: String,
    val lenderKey: String,
    val address: AddressData?
)
