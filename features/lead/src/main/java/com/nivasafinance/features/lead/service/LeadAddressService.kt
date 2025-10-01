package com.nivasafinance.features.lead.service

import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.CreateAddressRequest
import com.nivasafinance.features.address.dto.UpdateAddressRequest
import java.util.UUID

interface LeadAddressService {
    fun getLeadAddress(leadId: UUID): AddressResponse?
    fun createLeadAddress(leadId: UUID, addressRequest: CreateAddressRequest): AddressResponse
    fun updateLeadAddress(leadId: UUID, addressRequest: UpdateAddressRequest): AddressResponse
}
