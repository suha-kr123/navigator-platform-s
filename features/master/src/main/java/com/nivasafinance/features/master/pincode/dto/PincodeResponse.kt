package com.nivasafinance.features.master.pincode.dto

import java.util.UUID

data class PincodeResponse(
    val id: UUID?,
    val pincode: String,
    val area: String,
    val district: String?,
    val state: String?,
    val country: String?,
    val isServicable: Boolean
)
