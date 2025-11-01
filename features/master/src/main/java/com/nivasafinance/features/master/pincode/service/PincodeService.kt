package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.features.master.pincode.dto.PincodeResponse

interface PincodeService {
    fun getPincodeDetails(pincode: String): PincodeResponse
}
