package com.nivasafinance.features.lender.lender.dto

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData

data class LenderWithOfficesResponse(
    val name: String,
    val key: String,
    val offices: List<LenderOfficeReponseData>
)
