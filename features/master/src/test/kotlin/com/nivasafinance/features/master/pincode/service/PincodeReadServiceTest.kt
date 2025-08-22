package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.TestUtils.createTestPincodeEntity
import com.nivasafinance.TestUtils.createTestPincodeResponseDto
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
    private val expectedDto = createTestPincodeResponseDto(pincode = pincode)

    @BeforeEach
    fun setup() {
        pincodeReadService = PincodeReadServiceImpl(pincodeRepository)
        // Initialize the messageSource property in BaseNavigatorService
        pincodeReadService.messageSource = messageSource
    }

    @Nested
    @DisplayName("getByPincode Tests")
    inner class GetByPincodeTests {

        @Test
        @DisplayName("should return pincode details when found")
        fun `getByPincode should return pincode details when found`() {
            // Given
            every { pincodeRepository.findAllByPincode(pincode) } returns pincodeEntities

            // When
            val result = pincodeReadService.getByPincode(pincode)

            // Then
            assertNotNull(result)
            assertEquals(expectedDto.pincode, result.pincode)
            assertEquals(expectedDto.areas, result.areas)
            assertEquals(expectedDto.district, result.district)
            assertEquals(expectedDto.country, result.country)
            assertEquals(expectedDto.isServicable, result.isServicable)

            verify(exactly = 1) { pincodeRepository.findAllByPincode(pincode) }
        }

        @Test
        @DisplayName("should throw exception when pincode not found")
        fun `getByPincode should throw exception when pincode not found`() {
            // Given
            every { pincodeRepository.findAllByPincode(pincode) } returns emptyList()
            every { messageSource.getMessage(any(), any(), any()) } returns "Pincode not found"

            // When & Then
            assertThrows<PincodeNotFoundException> {
                pincodeReadService.getByPincode(pincode)
            }

            verify(exactly = 1) { pincodeRepository.findAllByPincode(pincode) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }

        @Test
        @DisplayName("should handle empty pincode")
        fun `getByPincode should handle empty pincode`() {
            // Given
            val emptyPincode = ""
            every { pincodeRepository.findAllByPincode(emptyPincode) } returns emptyList()
            every { messageSource.getMessage(any(), any(), any()) } returns "Pincode not found"

            // When & Then
            assertThrows<PincodeNotFoundException> {
                pincodeReadService.getByPincode(emptyPincode)
            }

            verify(exactly = 1) { pincodeRepository.findAllByPincode(emptyPincode) }
        }
    }
}
