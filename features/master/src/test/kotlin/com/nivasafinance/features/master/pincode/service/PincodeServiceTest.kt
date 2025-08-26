package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.MasterTestUtils.createTestPincodeData
import com.nivasafinance.MasterTestUtils.createTestPincodeResponse
import com.nivasafinance.features.master.pincode.service.impl.PincodeServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PincodeService Tests")
class PincodeServiceTest {

    private val pincodeReadService = mockk<PincodeReadService>()
    private lateinit var pincodeService: PincodeServiceImpl

    private val pincode = "123456"
    private val pincodeDataList = listOf(
        createTestPincodeData(pincode = pincode, area = "Mumbai Area 1"),
        createTestPincodeData(pincode = pincode, area = "Mumbai Area 2", isServicable = false)
    )
    private val expectedResponse = createTestPincodeResponse(
        pincode = pincode,
        areas = listOf("Mumbai Area 1", "Mumbai Area 2"),
        isServicable = true
    )

    @BeforeEach
    fun setup() {
        pincodeService = PincodeServiceImpl(pincodeReadService)
    }

    @Nested
    @DisplayName("getByPincode Tests")
    inner class GetByPincodeTests {

        @Test
        @DisplayName("should return pincode response when data found")
        fun `getByPincode should return pincode response when data found`() {
            // Given
            every { pincodeReadService.getPincodeDataByPincode(pincode) } returns pincodeDataList

            // When
            val result = pincodeService.getByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse.pincode, result.pincode)
            assertEquals(expectedResponse.areas, result.areas)
            assertEquals(expectedResponse.district, result.district)
            assertEquals(expectedResponse.country, result.country)
            assertEquals(expectedResponse.isServicable, result.isServicable)

            verify(exactly = 1) { pincodeReadService.getPincodeDataByPincode(pincode) }
        }

        @Test
        @DisplayName("should handle single pincode data")
        fun `getByPincode should handle single pincode data`() {
            // Given
            val singleData = listOf(createTestPincodeData(pincode = pincode, area = "Single Area"))
            every { pincodeReadService.getPincodeDataByPincode(pincode) } returns singleData

            // When
            val result = pincodeService.getByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(pincode, result.pincode)
            assertEquals(listOf("Single Area"), result.areas)
            assertEquals("Mumbai", result.district)
            assertEquals("India", result.country)
            assertEquals(true, result.isServicable)
        }

        @Test
        @DisplayName("should handle multiple areas with different serviceability")
        fun `getByPincode should handle multiple areas with different serviceability`() {
            // Given
            val mixedData = listOf(
                createTestPincodeData(pincode = pincode, area = "Serviceable Area", isServicable = true),
                createTestPincodeData(pincode = pincode, area = "Non-Serviceable Area", isServicable = false)
            )
            every { pincodeReadService.getPincodeDataByPincode(pincode) } returns mixedData

            // When
            val result = pincodeService.getByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(true, result.isServicable) // Should be true if any area is serviceable
            assertEquals(2, result.areas.size)
        }

        @Test
        @DisplayName("should handle null district and country")
        fun `getByPincode should handle null district and country`() {
            // Given
            val dataWithNulls = listOf(
                createTestPincodeData(pincode = pincode, area = "Test Area", district = null, country = null)
            )
            every { pincodeReadService.getPincodeDataByPincode(pincode) } returns dataWithNulls

            // When
            val result = pincodeService.getByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(pincode, result.pincode)
            assertEquals(listOf("Test Area"), result.areas)
            assertEquals(null, result.district)
            assertEquals(null, result.country)
        }

        @Test
        @DisplayName("should handle empty pincode data list")
        fun `getByPincode should handle empty pincode data list`() {
            // Given
            val emptyData = emptyList<com.nivasafinance.features.master.pincode.dto.PincodeData>()
            every { pincodeReadService.getPincodeDataByPincode(pincode) } returns emptyData

            // When
            val result = pincodeService.getByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(pincode, result.pincode)
            assertEquals(emptyList<String>(), result.areas)
            assertEquals(null, result.district)
            assertEquals(null, result.country)
            assertEquals(false, result.isServicable)
        }
    }
}
