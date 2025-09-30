package com.nivasafinance.features.lead.service

import java.util.UUID
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressUpdateRequest

interface LeadAddressService {
    fun getLeadAddress(leadId: UUID): AddressResponse?
    fun createLeadAddress(leadId: UUID, addressRequest: AddressCreateRequest): AddressResponse
    fun updateLeadAddress(leadId: UUID, addressRequest: AddressUpdateRequest): AddressResponse
}
