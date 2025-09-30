package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.features.master.pincode.dto.PincodeResponse

interface PincodeService {
    fun getPincodeDetails(pincode: String): List<PincodeResponse>
    fun isPincodeValid(pincode: String): Boolean
    fun getPincodeDetailsSafe(pincode: String): List<PincodeResponse>
}
