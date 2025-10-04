package com.nivasafinance.features.offices.service.impl

import com.nivasafinance.common.base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.offices.dto.OfficeResponse
import com.nivasafinance.features.offices.entity.Office
import com.nivasafinance.features.offices.exception.OfficeNotFoundException
import com.nivasafinance.features.offices.repository.OfficeRepository
import com.nivasafinance.features.offices.service.OfficeReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OfficeReadServiceImpl(
    private val officeRepository: OfficeRepository,
    private val addressRepository: AddressRepository
) : OfficeReadService, BaseNavigatorService() {

    override fun getOffice(id: UUID): OfficeResponse {
        val entity = officeRepository.findById(id).orElseThrow {
            OfficeNotFoundException(id, messageSource)
        }
        return toResponse(entity)
    }

    private fun toResponse(entity: Office): OfficeResponse {
        val address = entity.addressId?.let { id ->
            addressRepository.findById(id).orElse(null)
        }
        val addressResp = address?.let {
            AddressResponse(
                id = it.id,
                addressOne = it.addressOne,
                addressTwo = it.addressTwo,
                landmark = it.landmark,
                district = it.district,
                state = it.state,
                pincode = it.pincode,
                addressSource = it.addressSource.name,
                extData = it.extData
            )
        }
        return OfficeResponse(
            id = entity.id!!,
            name = entity.name,
            key = entity.key,
            code = entity.code,
            address = addressResp,
            parentId = entity.parentId
        )
    }
}
