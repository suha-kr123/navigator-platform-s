package com.nivasafinance.features.address.service.impl

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressReadService
import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class AddressWriteServiceImplTest {

    private val addressRepository = mockk<AddressRepository>()
    private val addressReadService = mockk<AddressReadService>()
    private val pincodeReadService = mockk<PincodeReadService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var addressWriteService: AddressWriteServiceImpl

    private val addressId = UUID.randomUUID()
    private val addressCreateRequest = AddressCreateRequest(
        addressOne = "123 Main St",
        addressTwo = "Apt 4B",
        landmark = "Near Park",
        district = "Mysore",
        state = "Karnataka",
        pincode = "570001",
        addressSource = "CUSTOMER"
    )
    private val addressEntity = Address(
        id = UUID.randomUUID(),
        addressOne = "123 Main St",
        addressTwo = "Apt 4B",
        landmark = "Near Park",
        district = "Mysore",
        state = "Karnataka",
        pincode = "570001",
        addressSource = "CUSTOMER"
    )
    private val savedAddressEntity = addressEntity.copy(id = addressId)
    private val savedAddressResponse = AddressResponse(
        id = addressId,
        addressOne = "123 Main St",
        addressTwo = "Apt 4B",
        landmark = "Near Park",
        district = "Mysore",
        state = "Karnataka",
        pincode = "570001",
        addressSource = "CUSTOMER"
    )

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
    fun `createAddress should save and return address`() {
        // Given
        every { pincodeReadService.getByPincode(any()) } throws IllegalArgumentException()
        every { modelMapper.map(addressCreateRequest, Address::class.java) } returns addressEntity
        every { addressRepository.save(addressEntity) } returns savedAddressEntity
        every { modelMapper.map(savedAddressEntity, AddressResponse::class.java) } returns savedAddressResponse

        // When
        val result = addressWriteService.createAddress(addressCreateRequest)

        // Then
        assertEquals(addressId, result.id)
        assertEquals(addressCreateRequest.addressOne, result.addressOne)
        assertEquals(addressCreateRequest.district, result.district)
        assertEquals(addressCreateRequest.state, result.state)
        assertEquals(addressCreateRequest.pincode, result.pincode)
        assertEquals(addressCreateRequest.addressSource, result.addressSource)

        verify(exactly = 1) { pincodeReadService.getByPincode("570001") }
        verify(exactly = 1) { modelMapper.map(addressCreateRequest, Address::class.java) }
        verify(exactly = 1) { addressRepository.save(addressEntity) }
        verify(exactly = 1) { modelMapper.map(savedAddressEntity, AddressResponse::class.java) }
    }

    @Test
    fun `createAddress should enrich district from pincode service`() {
        // Given
        val pincodeResponse = PincodeResponseDto(
            pincode = "570001",
            district = "Enriched District",
            country = "India",
            areas = listOf("Area 1"),
            isServicable = true
        )
        val enhancedRequest = addressCreateRequest.copy(district = "Enriched District")
        val enhancedEntity = addressEntity.copy(district = "Enriched District")
        every { pincodeReadService.getByPincode("570001") } returns pincodeResponse
        every { modelMapper.map(enhancedRequest, Address::class.java) } returns enhancedEntity
        every { addressRepository.save(enhancedEntity) } returns savedAddressEntity
        every { modelMapper.map(savedAddressEntity, AddressResponse::class.java) } returns savedAddressResponse

        // When
        val result = addressWriteService.createAddress(addressCreateRequest)

        // Then
        assertEquals(savedAddressResponse.id, result.id)
        verify(exactly = 1) { pincodeReadService.getByPincode("570001") }
        verify(exactly = 1) { modelMapper.map(enhancedRequest, Address::class.java) }
        verify(exactly = 1) { addressRepository.save(enhancedEntity) }
        verify(exactly = 1) { modelMapper.map(savedAddressEntity, AddressResponse::class.java) }
    }

    @Test
    fun `createAddress should handle pincode service failure gracefully`() {
        // Given
        every { pincodeReadService.getByPincode(any()) } throws IllegalArgumentException("Service unavailable")
        every { modelMapper.map(addressCreateRequest, Address::class.java) } returns addressEntity
        every { addressRepository.save(addressEntity) } returns savedAddressEntity
        every { modelMapper.map(savedAddressEntity, AddressResponse::class.java) } returns savedAddressResponse

        // When
        val result = addressWriteService.createAddress(addressCreateRequest)

        // Then
        assertEquals(addressId, result.id)
        assertEquals(addressCreateRequest.addressOne, result.addressOne)
        assertEquals(addressCreateRequest.district, result.district) // Should use original district

        verify(exactly = 1) { pincodeReadService.getByPincode("570001") }
        verify(exactly = 1) { modelMapper.map(addressCreateRequest, Address::class.java) }
        verify(exactly = 1) { addressRepository.save(addressEntity) }
        verify(exactly = 1) { modelMapper.map(savedAddressEntity, AddressResponse::class.java) }
    }

    @Test
    fun `updateAddress should update and return address when found`() {
        // Given
        val updateRequest = AddressUpdateRequest(
            addressOne = "Updated Street",
            addressTwo = "Updated Apt",
            landmark = "Updated Landmark",
            district = "Updated District",
            state = "Updated State",
            pincode = "570002",
            addressSource = "UPDATED_SOURCE"
        )
        val existingEntity = savedAddressEntity
        val updatedEntity = existingEntity.copy(
            addressOne = "Updated Street",
            addressTwo = "Updated Apt",
            landmark = "Updated Landmark",
            district = "Updated District",
            state = "Updated State",
            pincode = "570002",
            addressSource = "UPDATED_SOURCE"
        )
        val updatedResponse = AddressResponse(
            id = addressId,
            addressOne = "Updated Street",
            addressTwo = "Updated Apt",
            landmark = "Updated Landmark",
            district = "Updated District",
            state = "Updated State",
            pincode = "570002",
            addressSource = "UPDATED_SOURCE"
        )

        every { addressRepository.findById(addressId) } returns Optional.of(existingEntity)
        every { addressRepository.save(any<Address>()) } returns updatedEntity
        every { modelMapper.map(updatedEntity, AddressResponse::class.java) } returns updatedResponse

        // When
        val result = addressWriteService.updateAddress(addressId, updateRequest)

        // Then
        assertEquals(addressId, result.id)
        assertEquals("Updated Street", result.addressOne)
        assertEquals("Updated Apt", result.addressTwo)
        assertEquals("Updated Landmark", result.landmark)
        assertEquals("Updated District", result.district)
        assertEquals("Updated State", result.state)
        assertEquals("570002", result.pincode)
        assertEquals("UPDATED_SOURCE", result.addressSource)

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { addressRepository.save(any<Address>()) }
        verify(exactly = 1) { modelMapper.map(updatedEntity, AddressResponse::class.java) }
    }

    @Test
    fun `updateAddress should update all fields when provided`() {
        // Given
        val partialUpdateRequest = AddressUpdateRequest(
            addressOne = "New Street",
            addressTwo = null,
            landmark = null,
            district = "New District",
            state = null,
            pincode = "New Pincode",
            addressSource = null
        )
        val existingEntity = savedAddressEntity
        val updatedEntity = existingEntity.copy(
            addressOne = "New Street",
            addressTwo = null,
            landmark = null,
            district = "New District",
            state = null,
            pincode = "New Pincode",
            addressSource = null
        )
        val updatedResponse = AddressResponse(
            id = addressId,
            addressOne = "New Street",
            addressTwo = null,
            landmark = null,
            district = "New District",
            state = null,
            pincode = "New Pincode",
            addressSource = null
        )

        every { addressRepository.findById(addressId) } returns Optional.of(existingEntity)
        every { addressRepository.save(any<Address>()) } returns updatedEntity
        every { modelMapper.map(updatedEntity, AddressResponse::class.java) } returns updatedResponse

        // When
        val result = addressWriteService.updateAddress(addressId, partialUpdateRequest)

        // Then
        assertEquals(addressId, result.id)
        assertEquals("New Street", result.addressOne)
        assertEquals("New District", result.district)
        assertEquals("New Pincode", result.pincode)

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { addressRepository.save(any<Address>()) }
        verify(exactly = 1) { modelMapper.map(updatedEntity, AddressResponse::class.java) }
    }

    @Test
    fun `updateAddress should throw exception when address not found`() {
        // Given
        val updateRequest = AddressUpdateRequest(
            addressOne = "Updated",
            pincode = "570001"
        )

        every { addressRepository.findById(addressId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Address with id $addressId not found"

        // When & Then
        assertThrows<AddressNotFoundException> {
            addressWriteService.updateAddress(addressId, updateRequest)
        }

        verify(exactly = 1) { addressRepository.findById(addressId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }

    @Test
    fun `deleteAddress should delete and return address when found`() {
        // Given
        every { addressReadService.getAddress(addressId) } returns savedAddressResponse
        every { addressRepository.deleteById(addressId) } returns Unit

        // When
        val result = addressWriteService.deleteAddress(addressId)

        // Then
        assertEquals(savedAddressResponse, result)

        verify(exactly = 1) { addressReadService.getAddress(addressId) }
        verify(exactly = 1) { addressRepository.deleteById(addressId) }
    }

    @Test
    fun `deleteAddress should throw exception when address not found`() {
        // Given
        every { messageSource.getMessage(any(), any(), any()) } returns "Address not found"
        every { addressReadService.getAddress(addressId) } throws AddressNotFoundException(addressId, messageSource)

        // When & Then
        assertThrows<AddressNotFoundException> {
            addressWriteService.deleteAddress(addressId)
        }

        verify(exactly = 1) { addressReadService.getAddress(addressId) }
        verify(exactly = 0) { addressRepository.deleteById(any()) }
    }

    @Test
    fun `createAddress should handle null optional fields`() {
        // Given
        val minimalRequest = AddressCreateRequest(
            addressOne = "Main Street",
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001"
        )
        val minimalEntity = Address(
            id = UUID.randomUUID(),
            addressOne = "Main Street",
            addressTwo = null,
            landmark = null,
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001",
            addressSource = "CUSTOMER"
        )
        val savedMinimalEntity = minimalEntity.copy(id = addressId)
        val savedMinimalResponse = AddressResponse(
            id = addressId,
            addressOne = "Main Street",
            addressTwo = null,
            landmark = null,
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001",
            addressSource = "CUSTOMER"
        )

        every { pincodeReadService.getByPincode(any()) } throws IllegalArgumentException()
        every { modelMapper.map(minimalRequest, Address::class.java) } returns minimalEntity
        every { addressRepository.save(minimalEntity) } returns savedMinimalEntity
        every { modelMapper.map(savedMinimalEntity, AddressResponse::class.java) } returns savedMinimalResponse

        // When
        val result = addressWriteService.createAddress(minimalRequest)

        // Then
        assertEquals(addressId, result.id)
        assertEquals("Main Street", result.addressOne)
        assertEquals(null, result.addressTwo)
        assertEquals(null, result.landmark)

        verify(exactly = 1) { pincodeReadService.getByPincode("570001") }
        verify(exactly = 1) { modelMapper.map(minimalRequest, Address::class.java) }
        verify(exactly = 1) { addressRepository.save(minimalEntity) }
        verify(exactly = 1) { modelMapper.map(savedMinimalEntity, AddressResponse::class.java) }
    }
}
