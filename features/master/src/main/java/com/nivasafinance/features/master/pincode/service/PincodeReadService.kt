package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.features.master.pincode.dto.PincodeData

interface PincodeReadService {
    fun getPincodeDataByPincode(pincode: String): List<PincodeData>
}
