package com.nivasafinance.features.address.service

import com.nivasafinance.AddressTestUtils.createTestAddressCreateRequest
import com.nivasafinance.AddressTestUtils.createTestAddressResponse
import com.nivasafinance.AddressTestUtils.createTestAddressUpdateRequest
import com.nivasafinance.features.address.service.impl.AddressServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AddressService Tests")
class AddressServiceTest {

    private val addressReadService = mockk<AddressReadService>()
    private val addressWriteService = mockk<AddressWriteService>()
    private lateinit var addressService: AddressServiceImpl

    private val addressId = UUID.randomUUID()
    private val addressResponse = createTestAddressResponse(id = addressId)
    private val addressCreateRequest = createTestAddressCreateRequest()
    private val addressUpdateRequest = createTestAddressUpdateRequest()

    @BeforeEach
    fun setup() {
        addressService = AddressServiceImpl(addressReadService, addressWriteService)
    }

    @Nested
    @DisplayName("getAddress Tests")
    inner class GetAddressTests {

        @Test
        @DisplayName("should return address when found")
        fun `getAddress should return address when found`() {
            // Given
            every { addressReadService.getAddress(addressId) } returns addressResponse

            // When
            val result = addressService.getAddress(addressId)

            // Then
            assertNotNull(result)
            assertEquals(addressId, result.id)
            assertEquals(addressResponse.addressOne, result.addressOne)
            assertEquals(addressResponse.addressTwo, result.addressTwo)
            assertEquals(addressResponse.landmark, result.landmark)
            assertEquals(addressResponse.district, result.district)
            assertEquals(addressResponse.state, result.state)
            assertEquals(addressResponse.pincode, result.pincode)
            assertEquals(addressResponse.addressSource, result.addressSource)

            verify(exactly = 1) { addressReadService.getAddress(addressId) }
        }
    }

    @Nested
    @DisplayName("createAddress Tests")
    inner class CreateAddressTests {

        @Test
        @DisplayName("should create address successfully")
        fun `createAddress should create address successfully`() {
            // Given
            every { addressWriteService.createAddress(addressCreateRequest) } returns addressResponse

            // When
            val result = addressService.createAddress(addressCreateRequest)

            // Then
            assertNotNull(result)
            assertEquals(addressId, result.id)
            assertEquals(addressResponse.addressOne, result.addressOne)
            assertEquals(addressResponse.addressTwo, result.addressTwo)
            assertEquals(addressResponse.landmark, result.landmark)
            assertEquals(addressResponse.district, result.district)
            assertEquals(addressResponse.state, result.state)
            assertEquals(addressResponse.pincode, result.pincode)
            assertEquals(addressResponse.addressSource, result.addressSource)

            verify(exactly = 1) { addressWriteService.createAddress(addressCreateRequest) }
        }
    }

    @Nested
    @DisplayName("updateAddress Tests")
    inner class UpdateAddressTests {

        @Test
        @DisplayName("should update address successfully")
        fun `updateAddress should update address successfully`() {
            // Given
            every { addressWriteService.updateAddress(addressId, addressUpdateRequest) } returns addressResponse

            // When
            val result = addressService.updateAddress(addressId, addressUpdateRequest)

            // Then
            assertNotNull(result)
            assertEquals(addressId, result.id)
            assertEquals(addressResponse.addressOne, result.addressOne)
            assertEquals(addressResponse.addressTwo, result.addressTwo)
            assertEquals(addressResponse.landmark, result.landmark)
            assertEquals(addressResponse.district, result.district)
            assertEquals(addressResponse.state, result.state)
            assertEquals(addressResponse.pincode, result.pincode)
            assertEquals(addressResponse.addressSource, result.addressSource)

            verify(exactly = 1) { addressWriteService.updateAddress(addressId, addressUpdateRequest) }
        }
    }

    @Nested
    @DisplayName("deleteAddress Tests")
    inner class DeleteAddressTests {

        @Test
        @DisplayName("should delete address successfully")
        fun `deleteAddress should delete address successfully`() {
            // Given
            every { addressWriteService.deleteAddress(addressId) } returns addressResponse

            // When
            addressService.deleteAddress(addressId)

            // Then
            verify(exactly = 1) { addressWriteService.deleteAddress(addressId) }
        }
    }
}
