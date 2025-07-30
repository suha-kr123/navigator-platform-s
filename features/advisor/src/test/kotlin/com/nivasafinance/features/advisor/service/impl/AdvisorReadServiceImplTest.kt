package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorMobileNotFoundException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.service.PersonReadService
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

class AdvisorReadServiceImplTest {

    private lateinit var advisorRepository: AdvisorRepository
    private lateinit var personReadService: PersonReadService
    private lateinit var messageSource: MessageSource
    private lateinit var modelMapper: ModelMapper
    private lateinit var advisorReadService: AdvisorReadServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val mobileNo = "9876543210"

    private lateinit var advisor: Advisor
    private lateinit var personDto: PersonDto
    private lateinit var advisorDtoMapped: AdvisorDto

    @BeforeEach
    fun setup() {
        advisorRepository = mockk()
        personReadService = mockk()
        messageSource = mockk()
        modelMapper = mockk()

        advisor = Advisor(
            id = advisorId,
            personId = personId,
            advisorCode = "ADV123",
            status = AdvisorStatus.CREATED
        )

        personDto = PersonDto(
            id = personId,
            firstName = "John"
        )

        advisorDtoMapped = AdvisorDto(
            id = advisorId,
            advisorCode = "ADV123",
            status = AdvisorStatus.CREATED,
            personalDetails = personDto
        )

        advisorReadService = AdvisorReadServiceImpl(
            personReadService = personReadService,
            advisorRepository = advisorRepository
        ).apply {
            this.modelMapper = this@AdvisorReadServiceImplTest.modelMapper
            this.messageSource = this@AdvisorReadServiceImplTest.messageSource
        }
    }

    @Test
    fun `getAdvisor should return advisor when found`() {
        every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)
        every { personReadService.getPerson(personId) } returns personDto
        every { modelMapper.map(advisor, AdvisorDto::class.java) } returns advisorDtoMapped

        val result = advisorReadService.getAdvisor(advisorId)

        assertEquals(advisorDtoMapped.id, result.id)
        assertEquals(advisorDtoMapped.advisorCode, result.advisorCode)
        assertEquals(advisorDtoMapped.personalDetails?.id, result.personalDetails?.id)

        verify { advisorRepository.findById(advisorId) }
        verify { personReadService.getPerson(personId) }
        verify { modelMapper.map(advisor, AdvisorDto::class.java) }
    }

    @Test
    fun `getAdvisor should throw exception when not found`() {
        every { advisorRepository.findById(advisorId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

        assertThrows<AdvisorNotFoundException> {
            advisorReadService.getAdvisor(advisorId)
        }

        verify { advisorRepository.findById(advisorId) }
    }

    @Test
    fun `getAdvisorByMobileNo should return advisor when found`() {
        every { advisorRepository.findByPrimaryMobileNo(mobileNo) } returns advisor
        every { personReadService.getPerson(personId) } returns personDto
        every { modelMapper.map(advisor, AdvisorDto::class.java) } returns advisorDtoMapped

        val result = advisorReadService.getAdvisorByMobileNo(mobileNo)

        assertEquals(advisorDtoMapped.id, result.id)
        assertEquals(advisorDtoMapped.advisorCode, result.advisorCode)
        assertEquals(advisorDtoMapped.personalDetails?.id, result.personalDetails?.id)

        verify { advisorRepository.findByPrimaryMobileNo(mobileNo) }
        verify { personReadService.getPerson(personId) }
        verify { modelMapper.map(advisor, AdvisorDto::class.java) }
    }

    @Test
    fun `getAdvisorByMobileNo should throw exception when not found`() {
        every { advisorRepository.findByPrimaryMobileNo(mobileNo) } returns null
        every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

        assertThrows<AdvisorMobileNotFoundException> {
            advisorReadService.getAdvisorByMobileNo(mobileNo)
        }

        verify { advisorRepository.findByPrimaryMobileNo(mobileNo) }
    }
}
