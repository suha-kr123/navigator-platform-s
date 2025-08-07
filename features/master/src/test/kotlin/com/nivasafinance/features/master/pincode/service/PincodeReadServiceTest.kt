package com.nivasafinance.features.master.pincode.service

import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto
import com.nivasafinance.features.master.pincode.entity.PincodeEntity
import com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException
import com.nivasafinance.features.master.pincode.repository.PincodeRepository
import com.nivasafinance.features.master.pincode.service.impl.PincodeReadServiceImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.*

class PincodeReadServiceTest {

    private val pincodeRepository = mockk<PincodeRepository>()
    private val messageSource = mockk<MessageSource>()
    private val pincodeReadService = PincodeReadServiceImpl(pincodeRepository, messageSource)

    @Test
    fun `should return PincodeResponseDto for valid pincode with multiple areas`() {
        val pincode = "570017"

        val entities = listOf(
            PincodeEntity(
                id = UUID.randomUUID(),
                pincode = pincode,
                area = "Mysore North",
                district = "Mysore",
                country = "India",
                isServicable = true
            ),
            PincodeEntity(
                id = UUID.randomUUID(),
                pincode = pincode,
                area = "Mysore South",
                district = "Mysore",
                country = "India",
                isServicable = false,
            )
        )

        every { pincodeRepository.findAllByPincode(pincode) } returns entities

        val result = pincodeReadService.getByPincode(pincode)

        val expected = PincodeResponseDto(
            pincode = pincode,
            areas = listOf("Mysore North", "Mysore South"),
            district = "Mysore",
            country = "India",
            isServicable = true // at least one is true
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should throw PincodeNotFoundException for invalid pincode`() {
        val invalidPincode = "000000"
        val localizedMessage = "Pincode $invalidPincode not found"

        every { pincodeRepository.findAllByPincode(invalidPincode) } returns emptyList()
        every {
            messageSource.getMessage(
                "pincode.not.found",
                arrayOf(invalidPincode),
                any()
            )
        } returns localizedMessage

        val exception = assertThrows(PincodeNotFoundException::class.java) {
            pincodeReadService.getByPincode(invalidPincode)
        }

        assertEquals(localizedMessage, exception.message)
    }
}
