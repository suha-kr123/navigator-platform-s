package com.nivasafinance.features.address.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "addressCacheManager")
class AddressServiceImpl(
    private val addressRepository: AddressRepository
) : AddressService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "address"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAddress(id: UUID): AddressResponse {
        val address = addressRepository.findById(id)
            .orElseThrow { AddressNotFoundException(id, messageSource) }
        return mapEntityToResponse(address)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createAddress(request: AddressCreateRequest): AddressResponse {
        val address = Address(
            entityId = request.entityId,
            entityType = request.entityType,
            addressType = request.addressType,
            isPrimary = request.isPrimary,
            addressOne = request.addressOne,
            addressTwo = request.addressTwo,
            landmark = request.landmark,
            district = request.district,
            state = request.state,
            pincode = request.pincode,
            addressSource = request.addressSource
        )

        val savedAddress = addressRepository.save(address)
        return mapEntityToResponse(savedAddress)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    override fun updateAddress(id: UUID, request: AddressUpdateRequest): AddressResponse {
        val existingAddress = addressRepository.findById(id)
            .orElseThrow { AddressNotFoundException(id, messageSource) }

        val updatedAddress = existingAddress.copy(
            addressOne = request.addressOne ?: existingAddress.addressOne,
            addressTwo = request.addressTwo ?: existingAddress.addressTwo,
            landmark = request.landmark ?: existingAddress.landmark,
            district = request.district ?: existingAddress.district,
            state = request.state ?: existingAddress.state,
            pincode = request.pincode,
            addressSource = request.addressSource ?: existingAddress.addressSource
        )

        val savedAddress = addressRepository.save(updatedAddress)
        return mapEntityToResponse(savedAddress)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    override fun deleteAddress(id: UUID) {
        val address = addressRepository.findById(id)
            .orElseThrow { AddressNotFoundException(id, messageSource) }
        address.id?.let { addressRepository.deleteById(it) }
            ?: throw AddressNotFoundException(id, messageSource)
    }

    private fun mapEntityToResponse(address: Address): AddressResponse {
        return AddressResponse(
            id = address.id,
            entityId = address.entityId,
            entityType = address.entityType,
            addressType = address.addressType,
            isPrimary = address.isPrimary,
            addressOne = address.addressOne,
            addressTwo = address.addressTwo,
            landmark = address.landmark,
            district = address.district,
            state = address.state,
            pincode = address.pincode,
            addressSource = address.addressSource
        )
    }
}
