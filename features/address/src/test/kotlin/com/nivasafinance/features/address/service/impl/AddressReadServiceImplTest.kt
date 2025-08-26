package com.nivasafinance.features.address.service.impl

import com.nivasafinance.AddressTestUtils.createTestAddressEntity
import com.nivasafinance.AddressTestUtils.createTestAddressResponse
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AddressReadService Tests")
class AddressReadServiceImplTest {

    private val addressRepository = mockk<AddressRepository>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var addressReadService: AddressReadServiceImpl

    private val addressId = UUID.randomUUID()
    private val address = createTestAddressEntity(id = addressId)
    private val expectedResponse = createTestAddressResponse(id = addressId)

    @BeforeEach
    fun setup() {
        addressReadService = AddressReadServiceImpl(
            addressRepository,
            modelMapper,
            messageSource
        )
    }

    @Test
    @DisplayName("getAddress should return address when found")
    fun `getAddress should return address when found`() {
        // Given
        every { addressRepository.findById(addressId) } returns Optional.of(address)
        every { modelMapper.map(any(), eq(AddressResponse::class.java)) } returns expectedResponse

        // When
        val result = addressReadService.getAddress(addressId)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.addressOne, result.addressOne)
        assertEquals(expectedResponse.addressTwo, result.addressTwo)
        assertEquals(expectedResponse.landmark, result.landmark)
        assertEquals(expectedResponse.district, result.district)
        assertEquals(expectedResponse.state, result.state)
        assertEquals(expectedResponse.pincode, result.pincode)
        assertEquals(expectedResponse.addressSource, result.addressSource)

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { modelMapper.map(any(), eq(AddressResponse::class.java)) }
    }

    @Test
    @DisplayName("getAddress should throw exception when address not found")
    fun `getAddress should throw exception when address not found`() {
        // Given
        every { addressRepository.findById(addressId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Address not found"

        // When & Then
        assertThrows<AddressNotFoundException> {
            addressReadService.getAddress(addressId)
        }

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }
}
