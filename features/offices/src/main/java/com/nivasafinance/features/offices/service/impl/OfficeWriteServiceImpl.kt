package com.nivasafinance.features.offices.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.offices.dto.OfficeCreateRequest
import com.nivasafinance.features.offices.dto.OfficeResponse
import com.nivasafinance.features.offices.entity.Office
import com.nivasafinance.features.offices.repository.OfficeRepository
import com.nivasafinance.features.offices.service.OfficeCodeFactory
import com.nivasafinance.features.offices.service.OfficeWriteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OfficeWriteServiceImpl(
    private val officeRepository: OfficeRepository,
    private val addressRepository: AddressRepository,
    private val addressService: AddressService,
    private val officeCodeFactory: OfficeCodeFactory
) : OfficeWriteService, BaseNavigatorService() {

    @Transactional
    override fun createOffice(request: OfficeCreateRequest): OfficeResponse {
        // First create the address
        val addressResponse = addressService.createAddress(request.createAddressRequest)

        // Generate hierarchical code using factory
        val generatedCode = officeCodeFactory.generateOfficeCode(request.parentId)

        // Then create the office with the address ID and generated code
        val office = Office(
            name = request.name,
            key = request.key,
            code = generatedCode,
            addressId = addressResponse.id,
            parentId = request.parentId
        )

        val savedOffice = officeRepository.save(office)
        return toResponse(savedOffice)
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
