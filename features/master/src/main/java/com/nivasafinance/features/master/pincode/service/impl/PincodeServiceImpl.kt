package com.nivasafinance.features.master.pincode.service.impl

import com.nivasafinance.common.base.BaseNavigatorService
import com.nivasafinance.features.master.pincode.dto.PincodeResponse
import com.nivasafinance.features.master.pincode.exception.PincodeExceptionFactory
import com.nivasafinance.features.master.pincode.repository.PincodeRepository
import com.nivasafinance.features.master.pincode.service.PincodeService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PincodeServiceImpl(
    private val pincodeRepository: PincodeRepository
) : PincodeService, BaseNavigatorService() {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @Transactional(readOnly = true, noRollbackFor = [Exception::class])
    override fun getPincodeDetails(pincode: String): PincodeResponse {
        val pincodes = pincodeRepository.findAllByPincode(pincode)
        if (pincodes.isEmpty()) {
            throw PincodeExceptionFactory.notFound(pincode, messageSource)
        }
        val areas = pincodes.map { it.area }
        return PincodeResponse(
            pincode = pincodes.first().pincode,
            area = areas,
            district = pincodes.first().district,
            state = pincodes.first().state,
            country = pincodes.first().country,
            isServicable = pincodes.first().isServicable
        )
    }
}
