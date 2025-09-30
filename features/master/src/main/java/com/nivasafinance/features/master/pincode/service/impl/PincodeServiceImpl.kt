package com.nivasafinance.features.master.pincode.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.master.pincode.dto.PincodeResponse
import com.nivasafinance.features.master.pincode.entity.Pincode
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
    @Transactional(readOnly = true)
    override fun getPincodeDetails(pincode: String): List<PincodeResponse> {
        return try {
            PincodeExceptionFactory.validatePincode(pincode, messageSource)
            val pincodes = pincodeRepository.findAllByPincode(pincode)
            if (pincodes.isEmpty()) {
                throw PincodeExceptionFactory.notFound(pincode, messageSource)
            }
            pincodes.map { toPincodeResponse(it) }
        } catch (e: com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException) {
            throw e
        } catch (e: com.nivasafinance.features.master.pincode.exception.PincodeValidationException) {
            throw e
        } catch (e: com.nivasafinance.features.master.pincode.exception.PincodeOperationException) {
            throw e
        } catch (e: Exception) {
            throw PincodeExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @Transactional(readOnly = true)
    override fun isPincodeValid(pincode: String): Boolean {
        return try {
            PincodeExceptionFactory.validatePincode(pincode, messageSource)
            val pincodes = pincodeRepository.findAllByPincode(pincode)
            pincodes.isNotEmpty() && pincodes.any { it.isServicable }
        } catch (e: Exception) {
            // For validation, we intentionally return false for any exception
            false
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun getPincodeDetailsSafe(pincode: String): List<PincodeResponse> {
        return try {
            PincodeExceptionFactory.validatePincode(pincode, messageSource)
            val pincodes = pincodeRepository.findAllByPincode(pincode)
            pincodes.map { toPincodeResponse(it) }
        } catch (e: Exception) {
            // For safe operation, we intentionally return empty list for any exception
            emptyList()
        }
    }

    private fun toPincodeResponse(pincode: Pincode): PincodeResponse {
        return PincodeResponse(
            id = pincode.id,
            pincode = pincode.pincode,
            area = pincode.area,
            district = pincode.district,
            state = pincode.state,
            country = pincode.country,
            isServicable = pincode.isServicable
        )
    }
}
