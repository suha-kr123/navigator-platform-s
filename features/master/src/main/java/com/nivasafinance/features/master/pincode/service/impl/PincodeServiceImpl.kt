package com.nivasafinance.features.master.pincode.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.master.pincode.dto.PincodeData
import com.nivasafinance.features.master.pincode.dto.PincodeResponse
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import com.nivasafinance.features.master.pincode.service.PincodeService
import org.springframework.stereotype.Service

@Service
class PincodeServiceImpl(
    private val pincodeReadService: PincodeReadService
) : PincodeService, BaseNavigatorService() {

    override fun getByPincode(pincode: String): PincodeResponse {
        val pincodeDataList = pincodeReadService.getPincodeDataByPincode(pincode)
        return mapDataToResponse(pincode, pincodeDataList)
    }

    private fun mapDataToResponse(pincode: String, pincodeDataList: List<PincodeData>): PincodeResponse {
        return PincodeResponse(
            pincode = pincode,
            areas = pincodeDataList.map { it.area },
            district = pincodeDataList.firstOrNull()?.district,
            country = pincodeDataList.firstOrNull()?.country,
            isServicable = pincodeDataList.any { it.isServicable }
        )
    }
}
