package com.nivasafinance.features.address.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.service.AddressReadService
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.address.service.AddressWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AddressServiceImpl(
    private val addressReadService: AddressReadService,
    private val addressWriteService: AddressWriteService
) : AddressService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "address"
    }

    override fun getAddress(id: UUID): AddressResponse {
        return addressReadService.getAddress(id)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createAddress(request: AddressCreateRequest): AddressResponse {
        return addressWriteService.createAddress(request)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    override fun updateAddress(id: UUID, request: AddressUpdateRequest): AddressResponse {
        return addressWriteService.updateAddress(id, request)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    override fun deleteAddress(id: UUID) {
        addressWriteService.deleteAddress(id)
    }
}
