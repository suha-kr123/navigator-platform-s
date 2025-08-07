package com.nivasafinance.features.master.pincode.service.impl

import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto
import com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException
import com.nivasafinance.features.master.pincode.repository.PincodeRepository
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
class PincodeReadServiceImpl(
    private val pincodeRepository: PincodeRepository,
    private val messageSource: MessageSource
) : PincodeReadService {

    override fun getByPincode(pincode: String): PincodeResponseDto {
        val entities = pincodeRepository.findAllByPincode(pincode)

        if (entities.isEmpty()) {
            val message = messageSource.getMessage(
                "pincode.not.found",
                arrayOf(pincode),
                LocaleContextHolder.getLocale()
            )
            throw PincodeNotFoundException(message)
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
