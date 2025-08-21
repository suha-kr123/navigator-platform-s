package com.nivasafinance.features.address.service.impl

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressReadService
import com.nivasafinance.features.address.service.AddressWriteService
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import jakarta.transaction.Transactional
import org.javers.core.JaversBuilder.logger
import org.modelmapper.ModelMapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AddressWriteServiceImpl(
    private val addressRepository: AddressRepository,
    private val addressReadService: AddressReadService,
    private val pincodeReadService: PincodeReadService,
    private val modelMapper: ModelMapper,
    private val messageSource: MessageSource
) : AddressWriteService {

    companion object {
        private const val CACHE_NAME = "address"
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createAddress(request: AddressCreateRequest): AddressResponse {
        // Enhance address with pincode master details if available
        val enhancedRequest = try {
            val pincodeDetails = pincodeReadService.getByPincode(request.pincode)
            request.copy(
                district = pincodeDetails.district ?: request.district,
                state = request.state
            )
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to enhance address with pincode ${request.pincode}", e)
            request
        }

        val entity = modelMapper.map(enhancedRequest, Address::class.java)
        val savedAddress = addressRepository.save(entity)
        return modelMapper.map(savedAddress, AddressResponse::class.java)
    }

    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    override fun deleteAddress(id: UUID): AddressResponse {
        val existing = addressReadService.getAddress(id)
        addressRepository.deleteById(id)
        return existing
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    override fun updateAddress(id: UUID, request: AddressUpdateRequest): AddressResponse {
        val existingEntity = addressRepository.findById(id).orElseThrow {
            AddressNotFoundException(id, messageSource)
        }
        val updatedEntity = existingEntity.copy(
            addressOne = request.addressOne,
            addressTwo = request.addressTwo,
            landmark = request.landmark,
            district = request.district,
            state = request.state,
            pincode = request.pincode,
            addressSource = request.addressSource,
        )
        val savedAddress = addressRepository.save(updatedEntity)
        return modelMapper.map(savedAddress, AddressResponse::class.java)
    }
}
