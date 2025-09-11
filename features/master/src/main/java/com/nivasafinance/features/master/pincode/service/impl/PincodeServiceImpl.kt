package com.nivasafinance.features.master.pincode.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.master.pincode.dto.PincodeData
import com.nivasafinance.features.master.pincode.dto.PincodeResponse
import com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException
import com.nivasafinance.features.master.pincode.repository.PincodeRepository
import com.nivasafinance.features.master.pincode.service.PincodeService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheManager = "masterCacheManager")
class PincodeServiceImpl(
    private val pincodeRepository: PincodeRepository
) : PincodeService, BaseNavigatorService() {

    override fun getByPincode(pincode: String): PincodeResponse {
        val entities = pincodeRepository.findAllByPincode(pincode)

        if (entities.isEmpty()) {
            throw PincodeNotFoundException(pincode, messageSource)
        }

        val pincodeDataList = entities.map { PincodeData.fromEntity(it) }
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
