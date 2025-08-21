package com.nivasafinance.features.address.service.impl

import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.assertEquals

class AddressReadServiceImplTest {

    private val addressRepository = mockk<AddressRepository>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var addressReadService: AddressReadServiceImpl

    private val addressId = UUID.randomUUID()
    private val addressId2 = UUID.randomUUID()
    private val address = Address(
        id = addressId,
        addressOne = "123 Main St",
        addressTwo = "Apt 4B",
        landmark = "Near Park",
        district = "Mysore",
        state = "Karnataka",
        pincode = "570001",
        addressSource = "CUSTOMER"
    )
    private val address2 = Address(
        id = addressId2,
        addressOne = "456 Oak St",
        addressTwo = "Unit 7C",
        landmark = "Near Mall",
        district = "Bangalore",
        state = "Karnataka",
        pincode = "560001",
        addressSource = "ADVISOR"
    )
    private val expectedResponse = AddressResponse(
        id = addressId,
        addressOne = "123 Main St",
        addressTwo = "Apt 4B",
        landmark = "Near Park",
        district = "Mysore",
        state = "Karnataka",
        pincode = "570001",
        addressSource = "CUSTOMER"
    )
    private val expectedResponse2 = AddressResponse(
        id = addressId2,
        addressOne = "456 Oak St",
        addressTwo = "Unit 7C",
        landmark = "Near Mall",
        district = "Bangalore",
        state = "Karnataka",
        pincode = "560001",
        addressSource = "ADVISOR"
    )

    @BeforeEach
    fun setup() {
        addressReadService = AddressReadServiceImpl(
            addressRepository,
            modelMapper,
            messageSource
        )
    }

    @Test
    fun `getAddress should return address when found`() {
        // Given
        every { addressRepository.findById(addressId) } returns Optional.of(address)
        every { modelMapper.map(address, AddressResponse::class.java) } returns expectedResponse

        // When
        val result = addressReadService.getAddress(addressId)

        // Then
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.addressOne, result.addressOne)
        assertEquals(expectedResponse.addressTwo, result.addressTwo)
        assertEquals(expectedResponse.landmark, result.landmark)
        assertEquals(expectedResponse.district, result.district)
        assertEquals(expectedResponse.state, result.state)
        assertEquals(expectedResponse.pincode, result.pincode)
        assertEquals(expectedResponse.addressSource, result.addressSource)

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { modelMapper.map(address, AddressResponse::class.java) }
    }

    @Test
    fun `getAddress should throw exception when not found`() {
        // Given
        every { addressRepository.findById(addressId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Address not found"

        // When & Then
        assertThrows<AddressNotFoundException> {
            addressReadService.getAddress(addressId)
        }

        verify(exactly = 1) { addressRepository.findById(addressId) }
    }

    @Test
    fun `getAddresses should return addresses when found`() {
        // Given
        val ids = listOf(addressId, addressId2)
        val addresses = listOf(address, address2)
        val expectedResponses = listOf(expectedResponse, expectedResponse2)

        every { addressRepository.findAllById(ids) } returns addresses
        every { modelMapper.map(address, AddressResponse::class.java) } returns expectedResponse
        every { modelMapper.map(address2, AddressResponse::class.java) } returns expectedResponse2

        // When
        val result = addressReadService.getAddresses(ids)

        // Then
        assertEquals(2, result.size)
        assertEquals(expectedResponses, result)

        verify(exactly = 1) { addressRepository.findAllById(ids) }
        verify(exactly = 1) { modelMapper.map(address, AddressResponse::class.java) }
        verify(exactly = 1) { modelMapper.map(address2, AddressResponse::class.java) }
    }

    @Test
    fun `getAddresses should return empty list when no addresses found`() {
        // Given
        val ids = listOf(addressId, addressId2) // Non-empty list of IDs
        val addresses = listOf<Address>() // But no addresses found in repository

        every { addressRepository.findAllById(ids) } returns addresses

        // When
        val result = addressReadService.getAddresses(ids)

        // Then
        assertEquals(0, result.size)

        verify(exactly = 1) { addressRepository.findAllById(ids) }
    }

    @Test
    fun `getAddresses should return partial results when some addresses not found`() {
        // Given
        val ids = listOf(addressId, addressId2)
        val addresses = listOf(address) // Only one address found

        every { addressRepository.findAllById(ids) } returns addresses
        every { modelMapper.map(address, AddressResponse::class.java) } returns expectedResponse

        // When
        val result = addressReadService.getAddresses(ids)

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedResponse.id, result[0].id)

        verify(exactly = 1) { addressRepository.findAllById(ids) }
        verify(exactly = 1) { modelMapper.map(address, AddressResponse::class.java) }
    }

    @Test
    fun `getAddresses should return empty list when empty ids list provided`() {
        // Given
        val ids = listOf<UUID>()

        // When
        val result = addressReadService.getAddresses(ids)

        // Then
        assertEquals(0, result.size)

        verify(exactly = 0) { addressRepository.findAllById(any()) }
    }
}
