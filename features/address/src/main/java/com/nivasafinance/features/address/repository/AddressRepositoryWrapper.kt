package com.nivasafinance.features.address.repository

import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.exception.AddressExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AddressRepositoryWrapper(
    private val addressRepository: AddressRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(address: Address): Address {
        return try {
            addressRepository.save(address)
        } catch (e: Exception) {
            throw AddressExceptionFactory.createFailed(messageSource)
        }
    }

    fun findByIdWithException(id: UUID): Address {
        return try {
            addressRepository.findById(id).orElseThrow {
                AddressExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: Exception) {
            throw AddressExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllWithException(): List<Address> {
        return try {
            addressRepository.findAll()
        } catch (e: Exception) {
            throw AddressExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            if (!addressRepository.existsById(id)) {
                throw AddressExceptionFactory.notFound(id, messageSource)
            }
            addressRepository.deleteById(id)
        } catch (e: Exception) {
            throw AddressExceptionFactory.deleteFailed(messageSource)
        }
    }
}
