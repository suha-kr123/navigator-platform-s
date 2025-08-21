package com.nivasafinance.features.master.pincode.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto
import com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException
import com.nivasafinance.features.master.pincode.repository.PincodeRepository
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PincodeReadServiceImpl(
    private val pincodeRepository: PincodeRepository
) : PincodeReadService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "pincode"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#pincode")
    override fun getByPincode(pincode: String): PincodeResponseDto {
        val entities = pincodeRepository.findAllByPincode(pincode)

        if (entities.isEmpty()) {
            throw PincodeNotFoundException(pincode, messageSource)
        }

        return PincodeResponseDto(
            pincode = pincode,
            areas = entities.map { it.area },
            district = entities.first().district,
            country = entities.first().country,
            isServicable = entities.any { it.isServicable }
        )
    }
}
