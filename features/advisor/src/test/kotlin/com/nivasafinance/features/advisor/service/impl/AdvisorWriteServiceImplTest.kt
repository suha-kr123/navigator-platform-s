package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorMobileAlreadyExistsException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.person.dto.MobileNumberDetails
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.service.PersonWriteService
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

class AdvisorWriteServiceImplTest {

    private val advisorRepository = mockk<AdvisorRepository>()
    private val personWriteService = mockk<PersonWriteService>()
    private val advisorReadService = mockk<AdvisorReadService>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var advisorWriteService: AdvisorWriteServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val mobileNo = "9876543210"

    @BeforeEach
    fun setup() {
        advisorWriteService = AdvisorWriteServiceImpl(
            advisorRepository,
            personWriteService,
            advisorReadService
        ).apply {
            this.modelMapper = ModelMapper()
            this.messageSource = this@AdvisorWriteServiceImplTest.messageSource
        }
    }

    @Test
    fun `createAdvisor should create advisor when mobile not exists`() {
        val personDto = PersonDto(
            firstName = "John",
            mobileNumber = MobileNumberDetails(mobileNo)
        )
        val advisorDto = AdvisorDto(personalDetails = personDto)
        val savedPersonDto = personDto.copy(id = personId)

        every { advisorReadService.getAdvisorByMobileNo(mobileNo) } throws Exception()
        every { personWriteService.savePerson(personDto) } returns savedPersonDto
        every { advisorRepository.save(any<Advisor>()) } returns mockk<Advisor> {
            every { id } returns advisorId
            every { advisorCode } returns "ADV123"
            every { status } returns AdvisorStatus.CREATED
        }

        val result = advisorWriteService.createAdvisor(advisorDto)

        assertEquals(savedPersonDto, result.personalDetails)
        verify { personWriteService.savePerson(personDto) }
        verify { advisorRepository.save(any<Advisor>()) }
    }

    @Test
    fun `createAdvisor should throw exception when mobile already exists`() {
        val personDto = PersonDto(mobileNumber = MobileNumberDetails(mobileNo))
        val advisorDto = AdvisorDto(personalDetails = personDto)
        val existingAdvisor = AdvisorDto(id = UUID.randomUUID())

        every { advisorReadService.getAdvisorByMobileNo(mobileNo) } returns existingAdvisor
        every { messageSource.getMessage(any(), any(), any()) } returns "Mobile exists"

        assertThrows<AdvisorMobileAlreadyExistsException> {
            advisorWriteService.createAdvisor(advisorDto)
        }
    }

    @Test
    fun `deleteAdvisor should delete advisor and person`() {
        val personDto = PersonDto(id = personId)
        val advisorDto = AdvisorDto(id = advisorId, personalDetails = personDto)

        every { advisorReadService.getAdvisor(advisorId) } returns advisorDto
        every { personWriteService.deletePerson(personId) } returns Unit
        every { advisorRepository.deleteById(advisorId) } returns Unit

        val result = advisorWriteService.deleteAdvisor(advisorId)

        assertEquals(advisorDto, result)
        verify { personWriteService.deletePerson(personId) }
        verify { advisorRepository.deleteById(advisorId) }
    }

    @Test
    fun `updateAdvisor should update advisor and person`() {
        val advisor = Advisor(advisorId, personId, "ADV123", AdvisorStatus.CREATED)
        val personDto = PersonDto(id = personId, firstName = "Updated")
        val advisorDto = AdvisorDto(personalDetails = personDto)

        every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)
        every { personWriteService.updatePerson(personId, personDto) } returns personDto
        every { advisorRepository.save(advisor) } returns advisor

        val result = advisorWriteService.updateAdvisor(advisorId, advisorDto)

        assertEquals(personDto, result.personalDetails)
        verify { personWriteService.updatePerson(personId, personDto) }
        verify { advisorRepository.save(advisor) }
    }

    @Test
    fun `updateAdvisor should throw exception when advisor not found`() {
        val advisorDto = AdvisorDto()

        every { advisorRepository.findById(advisorId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Not found"

        assertThrows<AdvisorNotFoundException> {
            advisorWriteService.updateAdvisor(advisorId, advisorDto)
        }
    }
}
