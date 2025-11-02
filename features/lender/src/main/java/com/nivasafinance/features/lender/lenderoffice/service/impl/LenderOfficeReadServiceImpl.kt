package com.nivasafinance.features.lender.lenderoffice.service.impl

import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class LenderOfficeReadServiceImpl(
    private val lenderOfficeRepositoryWrapper: LenderOfficeRepositoryWrapper,
    private val addressService: AddressService
) : LenderOfficeReadService {

    override fun getByKey(key: String): LenderOfficeReponseData {
        val lenderOffice = lenderOfficeRepositoryWrapper.findByKeyWithException(key)
        return lenderOffice.toResponse()
    }

    override fun getById(id: UUID): LenderOfficeReponseData {
        val lenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id)
        return lenderOffice.toResponse()
    }

    override fun getByLenderKeyAndStatus(
        lenderKey: String,
        status: LenderOfficeStatus
    ): List<LenderOfficeReponseData> {
        return lenderOfficeRepositoryWrapper.findByLenderKeyAndStatus(lenderKey, status)
            .map { it.toResponse() }
    }

    private fun LenderOffice.toResponse(): LenderOfficeReponseData {
        return LenderOfficeReponseData(
            id = checkNotNull(this.id) { "Lender office ID cannot be null" },
            name = this.name,
            key = this.key,
            lenderKey = this.lenderKey,
            address = this.addressData
        )
    }
}
