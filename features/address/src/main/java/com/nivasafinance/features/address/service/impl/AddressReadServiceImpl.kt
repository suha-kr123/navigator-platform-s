package com.nivasafinance.features.address.service.impl

import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressReadService
import org.modelmapper.ModelMapper
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AddressReadServiceImpl(
    private val addressRepository: AddressRepository,
    private val modelMapper: ModelMapper,
    private val messageSource: MessageSource
) : AddressReadService {

    companion object {
        private const val CACHE_NAME = "address"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAddress(id: UUID): AddressResponse {
        val address = addressRepository.findById(id)
            .orElseThrow { AddressNotFoundException(id, messageSource) }
        return modelMapper.map(address, AddressResponse::class.java)
    }
}
