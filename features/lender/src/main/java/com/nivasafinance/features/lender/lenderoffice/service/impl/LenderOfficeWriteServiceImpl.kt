package com.nivasafinance.features.lender.lenderoffice.service.impl

import com.nivasafinance.common.dto.AddressData
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
        //todo set address

        val lenderOffice = LenderOffice(
            name = lenderOfficeData.name,
            key = lenderOfficeData.key,
            lenderKey = lenderOfficeData.lenderKey,
            addressData = null,
            status = lenderOfficeData.status
        )
        val savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(lenderOffice)
        return savedLenderOffice.toResponse()
    }

    override fun update(id: UUID, lenderOfficeData: LenderOfficeRequestData): LenderOfficeReponseData {
        val existingLenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id)

        //todo set address

        val updatedLenderOffice = existingLenderOffice.copy(
            name = lenderOfficeData.name,
            key = lenderOfficeData.key,
            lenderKey = lenderOfficeData.lenderKey,
            addressData = null,
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
