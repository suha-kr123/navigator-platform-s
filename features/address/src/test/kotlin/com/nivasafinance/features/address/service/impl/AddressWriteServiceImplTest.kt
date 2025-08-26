package com.nivasafinance.features.address.service.impl

import com.nivasafinance.AddressTestUtils.createTestAddressCreateRequest
import com.nivasafinance.AddressTestUtils.createTestAddressEntity
import com.nivasafinance.AddressTestUtils.createTestAddressResponse
import com.nivasafinance.AddressTestUtils.createTestPincodeData
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressReadService
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AddressWriteService Tests")
class AddressWriteServiceImplTest {

    private val addressRepository = mockk<AddressRepository>()
    private val addressReadService = mockk<AddressReadService>()
    private val pincodeReadService = mockk<PincodeReadService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var addressWriteService: AddressWriteServiceImpl

    private val addressId = UUID.randomUUID()
    private val addressCreateRequest = createTestAddressCreateRequest()
    private val addressEntity = createTestAddressEntity()
    private val savedAddressEntity = createTestAddressEntity(id = addressId)
    private val savedAddressResponse = createTestAddressResponse(id = addressId)

    @BeforeEach
    fun setup() {
        addressWriteService = AddressWriteServiceImpl(
            addressRepository,
            addressReadService,
            pincodeReadService,
            modelMapper,
            messageSource
        )
    }

    @Test
    @DisplayName("createAddress should create address with pincode validation")
    fun `createAddress should create address with pincode validation`() {
        // Given
        val pincodeData = listOf(createTestPincodeData())
        every { pincodeReadService.getPincodeDataByPincode(any()) } returns pincodeData
        every {
            modelMapper.map(
                any(),
                eq(com.nivasafinance.features.address.entity.Address::class.java)
            )
        } returns addressEntity
        every { addressRepository.save(any()) } returns savedAddressEntity
        every {
            modelMapper.map(
                any(),
                eq(com.nivasafinance.features.address.dto.AddressResponse::class.java)
            )
        } returns savedAddressResponse

        // When
        val result = addressWriteService.createAddress(addressCreateRequest)

        // Then
        assertNotNull(result)
        assertEquals(addressId, result.id)

        verify(exactly = 1) { pincodeReadService.getPincodeDataByPincode("570001") }
        verify(exactly = 1) {
            modelMapper.map(
                any(),
                eq(com.nivasafinance.features.address.entity.Address::class.java)
            )
        }
        verify(exactly = 1) { addressRepository.save(any()) }
    }

    @Test
    @DisplayName("createAddress should create address when pincode not found")
    fun `createAddress should create address when pincode not found`() {
        // Given
        every { pincodeReadService.getPincodeDataByPincode(any()) } returns emptyList()
        every {
            modelMapper.map(
                any(),
                eq(com.nivasafinance.features.address.entity.Address::class.java)
            )
        } returns addressEntity
        every { addressRepository.save(any()) } returns savedAddressEntity
        every {
            modelMapper.map(
                any(),
                eq(com.nivasafinance.features.address.dto.AddressResponse::class.java)
            )
        } returns savedAddressResponse

        // When
        val result = addressWriteService.createAddress(addressCreateRequest)

        // Then
        assertNotNull(result)
        assertEquals(addressId, result.id)

        verify(exactly = 1) { pincodeReadService.getPincodeDataByPincode("570001") }
        verify(exactly = 1) {
            modelMapper.map(
                any(),
                eq(com.nivasafinance.features.address.entity.Address::class.java)
            )
        }
        verify(exactly = 1) { addressRepository.save(any()) }
    }

    @Test
    @DisplayName("deleteAddress should delete address and return response")
    fun `deleteAddress should delete address and return response`() {
        // Given
        every { addressReadService.getAddress(addressId) } returns savedAddressResponse
        every { addressRepository.deleteById(any()) } returns Unit

        // When
        val result = addressWriteService.deleteAddress(addressId)

        // Then
        assertNotNull(result)
        assertEquals(addressId, result.id)

        verify(exactly = 1) { addressReadService.getAddress(addressId) }
        verify(exactly = 1) { addressRepository.deleteById(addressId) }
    }

    @Test
    @DisplayName("updateAddress should update address when found")
    fun `updateAddress should update address when found`() {
        // Given
        val updateRequest = com.nivasafinance.features.address.dto.AddressUpdateRequest(
            pincode = "570002"
        )
        val existingEntity = savedAddressEntity
        val updatedEntity = existingEntity.copy(pincode = "570002")
        val updatedResponse = createTestAddressResponse(
            id = addressId,
            pincode = "570002"
        )

        every { addressRepository.findById(addressId) } returns Optional.of(existingEntity)
        every { addressRepository.save(any()) } returns updatedEntity
        every {
            modelMapper.map(
                updatedEntity,
                eq(com.nivasafinance.features.address.dto.AddressResponse::class.java)
            )
        } returns updatedResponse

        // When
        val result = addressWriteService.updateAddress(addressId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals(addressId, result.id)
        assertEquals("570002", result.pincode)

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { addressRepository.save(any()) }
        verify(exactly = 1) {
            modelMapper.map(
                updatedEntity,
                eq(com.nivasafinance.features.address.dto.AddressResponse::class.java)
            )
        }
    }

    @Test
    @DisplayName("updateAddress should throw exception when address not found")
    fun `updateAddress should throw exception when address not found`() {
        // Given
        val updateRequest = com.nivasafinance.features.address.dto.AddressUpdateRequest(
            pincode = "570002"
        )
        every { addressRepository.findById(addressId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Address not found"

        // When & Then
        assertThrows<AddressNotFoundException> {
            addressWriteService.updateAddress(addressId, updateRequest)
        }

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }
}
