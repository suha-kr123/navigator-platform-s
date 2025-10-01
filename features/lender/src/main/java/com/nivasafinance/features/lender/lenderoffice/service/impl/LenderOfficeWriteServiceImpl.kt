package com.nivasafinance.features.lender.lenderoffice.service.impl

import com.nivasafinance.features.address.service.AddressService
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
    private val addressService: AddressService,
    private val lenderOfficeRepositoryWrapper: LenderOfficeRepositoryWrapper
) : LenderOfficeWriteService {

    override fun create(lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData {
        val addressResponse = addressService.createAddress(lenderOfficeData.createAddressRequest)
        val addressId = checkNotNull(addressResponse.id) { "Address ID cannot be null" }

        val lenderOffice = LenderOffice(
            name = lenderOfficeData.name,
            key = lenderOfficeData.key,
            lenderKey = lenderOfficeData.lenderKey,
            addressId = addressId,
            status = lenderOfficeData.status
        )
        val savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(lenderOffice)
        return savedLenderOffice.toResponse()
    }

    override fun update(id: UUID, lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData {
        val existingLenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id)

        // Create new address for update
        val addressResponse = addressService.createAddress(lenderOfficeData.createAddressRequest)
        val addressId = checkNotNull(addressResponse.id) { "Address ID cannot be null" }

        val updatedLenderOffice = existingLenderOffice.copy(
            name = lenderOfficeData.name,
            key = lenderOfficeData.key,
            lenderKey = lenderOfficeData.lenderKey,
            addressId = addressId,
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
