package com.nivasafinance.features.lender.lenderoffice.service.impl

import com.nivasafinance.common.dto.AddressData
import com.nivasafinance.common.dto.AddressRequest
import com.nivasafinance.features.address.service.AddressDataService
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeWriteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LenderOfficeWriteServiceImpl(
    private val addressDataService: AddressDataService,
    private val lenderOfficeRepositoryWrapper: LenderOfficeRepositoryWrapper
) : LenderOfficeWriteService {

    override fun create(lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData {
        // Use common address data service for address creation
        val addressData = lenderOfficeData.createAddressRequest?.let { addressRequest ->
            val addressReq = AddressRequest(
                addressRequest.addressLineOne,
                addressRequest.addressLineTwo,
                addressRequest.pincode,
                null // area field
            )
            addressDataService.createAddressData(addressReq)
        }

        val lenderOffice = LenderOffice(
            name = lenderOfficeData.name,
            key = lenderOfficeData.key,
            lenderKey = lenderOfficeData.lenderKey,
            addressData = addressData,
            status = lenderOfficeData.status
        )
        val savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(lenderOffice)
        return savedLenderOffice.toResponse()
    }

    override fun update(id: UUID, lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData {
        val existingLenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id)

        // Use common address data service for address creation
        val addressData = lenderOfficeData.createAddressRequest?.let { addressRequest ->
            val addressReq = AddressRequest(
                addressRequest.addressLineOne,
                addressRequest.addressLineTwo,
                addressRequest.pincode,
                null // area field
            )
            addressDataService.createAddressData(addressReq)
        }

        val updatedLenderOffice = existingLenderOffice.copy(
            name = lenderOfficeData.name,
            key = lenderOfficeData.key,
            lenderKey = lenderOfficeData.lenderKey,
            addressData = addressData,
            status = lenderOfficeData.status
        )
        val savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(updatedLenderOffice)
        return savedLenderOffice.toResponse()
    }

    override fun delete(id: UUID) {
        lenderOfficeRepositoryWrapper.deleteByIdWithException(id)
    }

    private fun LenderOffice.toResponse(): LenderOfficeReponseData {
        return LenderOfficeReponseData(
            id = checkNotNull(this.id) { "Lender office ID cannot be null" },
            name = this.name,
            key = this.key,
            lenderKey = this.lenderKey,
            address = null // Address details will be populated by LenderOfficeReadServiceImpl
        )
    }
}
