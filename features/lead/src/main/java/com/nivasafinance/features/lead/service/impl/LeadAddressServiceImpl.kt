package com.nivasafinance.features.lead.service.impl

import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.CreateAddressRequest
import com.nivasafinance.features.address.dto.UpdateAddressRequest
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadAddressService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LeadAddressServiceImpl(
    private val addressService: AddressService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val messageSource: MessageSource
) : LeadAddressService {

    override fun getLeadAddress(leadId: UUID): AddressResponse? {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        return lead.addressId?.let { addressId ->
            addressService.getAddressById(addressId)
        }
    }

    override fun createLeadAddress(
        leadId: UUID,
        addressRequest: CreateAddressRequest
    ): AddressResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        val addressResponse = addressService.createAddress(addressRequest)

        // Update lead with the new address ID
        lead.addressId = addressResponse.id
        leadRepositoryWrapper.saveWithException(lead)

        return addressResponse
    }

    override fun updateLeadAddress(
        leadId: UUID,
        addressRequest: UpdateAddressRequest
    ): AddressResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        val addressId = lead.addressId
            ?: throw LeadExceptionFactory.addressNotFoundForLead(leadId, messageSource)

        return addressService.updateAddress(addressId, addressRequest)
    }
}
