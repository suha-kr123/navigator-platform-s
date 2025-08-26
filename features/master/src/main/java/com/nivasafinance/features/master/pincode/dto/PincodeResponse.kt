package com.nivasafinance.features.master.pincode.dto

data class PincodeResponse(
    val pincode: String,
    val areas: List<String>,
    val district: String?,
    val country: String?,
    val isServicable: Boolean
)
