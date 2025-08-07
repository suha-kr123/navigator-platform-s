package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto

interface PincodeReadService {
    fun getByPincode(pincode: String): PincodeResponseDto
}
