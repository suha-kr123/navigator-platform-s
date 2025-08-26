package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.MasterTestUtils.createTestPincodeData
import com.nivasafinance.MasterTestUtils.createTestPincodeEntity
import com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException
import com.nivasafinance.features.master.pincode.repository.PincodeRepository
import com.nivasafinance.features.master.pincode.service.impl.PincodeReadServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PincodeReadService Tests")
class PincodeReadServiceTest {

    private val pincodeRepository = mockk<PincodeRepository>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var pincodeReadService: PincodeReadServiceImpl

    private val pincode = "123456"
    private val pincodeEntities = listOf(createTestPincodeEntity(pincode = pincode))
    private val expectedData = listOf(createTestPincodeData(pincode = pincode))

    @BeforeEach
    fun setup() {
        pincodeReadService = PincodeReadServiceImpl(pincodeRepository)
        // Initialize the messageSource property in BaseNavigatorService
        pincodeReadService.messageSource = messageSource
    }

    @Nested
    @DisplayName("getPincodeDataByPincode Tests")
    inner class GetPincodeDataByPincodeTests {

        @Test
        @DisplayName("should return pincode data when found")
        fun `getPincodeDataByPincode should return pincode data when found`() {
            // Given
            every { pincodeRepository.findAllByPincode(pincode) } returns pincodeEntities

            // When
            val result = pincodeReadService.getPincodeDataByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(expectedData.size, result.size)
            assertEquals(expectedData.first().pincode, result.first().pincode)
            assertEquals(expectedData.first().area, result.first().area)
            assertEquals(expectedData.first().district, result.first().district)
            assertEquals(expectedData.first().country, result.first().country)
            assertEquals(expectedData.first().isServicable, result.first().isServicable)

            verify(exactly = 1) { pincodeRepository.findAllByPincode(pincode) }
        }

        @Test
        @DisplayName("should throw exception when pincode not found")
        fun `getPincodeDataByPincode should throw exception when pincode not found`() {
            // Given
            every { pincodeRepository.findAllByPincode(pincode) } returns emptyList()
            every { messageSource.getMessage(any(), any(), any()) } returns "Pincode not found"

            // When & Then
            assertThrows<PincodeNotFoundException> {
                pincodeReadService.getPincodeDataByPincode(pincode)
            }

            verify(exactly = 1) { pincodeRepository.findAllByPincode(pincode) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }

        @Test
        @DisplayName("should handle empty pincode")
        fun `getPincodeDataByPincode should handle empty pincode`() {
            // Given
            val emptyPincode = ""
            every { pincodeRepository.findAllByPincode(emptyPincode) } returns emptyList()
            every { messageSource.getMessage(any(), any(), any()) } returns "Pincode not found"

            // When & Then
            assertThrows<PincodeNotFoundException> {
                pincodeReadService.getPincodeDataByPincode(emptyPincode)
            }

            verify(exactly = 1) { pincodeRepository.findAllByPincode(emptyPincode) }
        }
    }
}
