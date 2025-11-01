package com.nivasafinance.features.master.pincode.dto

data class PincodeResponse(
    val pincode: String,
    val area: List<String>,
    val district: String?,
    val state: String?,
    val country: String?,
    val isServicable: Boolean
)
