package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import com.nivasafinance.features.person.service.PersonWriteService
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

@DisplayName("PersonServiceImpl Tests")
class PersonServiceImplTest {

    private val personReadService = mockk<PersonReadService>()
    private val personWriteService = mockk<PersonWriteService>()
    private val personRepository = mockk<PersonRepository>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personService: PersonServiceImpl

    private val personId = UUID.randomUUID()
    private val personData = PersonData(
        id = personId,
        firstName = "John",
        middleName = null,
        lastName = "Doe",
        mobileNumbers = null,
        email = "john.doe@example.com",
        dateOfBirth = null,
        gender = data.enums.Gender.MALE,
        dataExt = null
    )
    private val personResponse = PersonResponse(
        id = personId,
        firstName = "John",
        lastName = "Doe",
        mobileNumbers = null,
        email = "john.doe@example.com",
        gender = data.enums.Gender.MALE
    )

    @BeforeEach
    fun setup() {
        personService = PersonServiceImpl(personReadService, personWriteService, personRepository)

        val modelMapperField = personService.javaClass.superclass.getDeclaredField("modelMapper")
        modelMapperField.isAccessible = true
        modelMapperField.set(personService, modelMapper)

        val messageSourceField = personService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(personService, messageSource)

        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
    }

    @Test
    @DisplayName("should get person successfully")
    fun `getPerson should get person successfully`() {
        every { personReadService.getPerson(personId) } returns personData
        every { modelMapper.map(personData, PersonResponse::class.java) } returns personResponse

        val result = personService.getPerson(personId)

        assertNotNull(result)
        assertEquals(personResponse, result)
        verify(exactly = 1) { personReadService.getPerson(personId) }
    }

    @Test
    @DisplayName("should create person successfully")
    fun `createPerson should create person successfully`() {
        val request = PersonCreateRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            gender = data.enums.Gender.MALE
        )
        val person = com.nivasafinance.features.person.entity.Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            gender = data.enums.Gender.MALE
        )
        every { personWriteService.createPerson(request) } returns personId
        every { personRepository.findById(personId) } returns Optional.of(person)
        every { modelMapper.map(personData, PersonResponse::class.java) } returns personResponse

        val result = personService.createPerson(request)

        assertNotNull(result)
        assertEquals(personResponse, result)
        verify(exactly = 1) { personWriteService.createPerson(request) }
        verify(exactly = 1) { personRepository.findById(personId) }
    }

    @Test
    @DisplayName("should throw exception when person not found after creation")
    fun `createPerson should throw exception when person not found after creation`() {
        val request = PersonCreateRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            gender = data.enums.Gender.MALE
        )
        every { personWriteService.createPerson(request) } returns personId
        every { personRepository.findById(personId) } returns Optional.empty()

        assertThrows<PersonNotFoundException> {
            personService.createPerson(request)
        }

        verify(exactly = 1) { personWriteService.createPerson(request) }
        verify(exactly = 1) { personRepository.findById(personId) }
    }

    @Test
    @DisplayName("should update person successfully")
    fun `updatePerson should update person successfully`() {
        val request = PersonUpdateRequest(
            firstName = "Jane",
            lastName = "Doe"
        )
        every { personWriteService.updatePerson(personId, request) } returns Unit
        every { personReadService.getPerson(personId) } returns personData
        every { modelMapper.map(personData, PersonResponse::class.java) } returns personResponse

        val result = personService.updatePerson(personId, request)

        assertNotNull(result)
        assertEquals(personResponse, result)
        verify(exactly = 1) { personWriteService.updatePerson(personId, request) }
        verify(exactly = 1) { personReadService.getPerson(personId) }
    }

    @Test
    @DisplayName("should delete person successfully")
    fun `deletePerson should delete person successfully`() {
        every { personWriteService.deletePerson(personId) } returns Unit

        personService.deletePerson(personId)

        verify(exactly = 1) { personWriteService.deletePerson(personId) }
    }
}
