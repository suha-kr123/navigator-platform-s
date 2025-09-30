package com.nivasafinance.features.lender.lenderoffice.dto

import com.nivasafinance.features.address.dto.AddressResponse
import java.util.UUID

data class LenderOfficeReponseData(
    val id: UUID,
    val name: String,
    val key: String,
    val lenderKey: String,
    val address: AddressResponse?
)
