package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsCreateRequest
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsResponse
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsCreateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsUpdateRequest
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

    // Employment Details Tests

    @Test
    @DisplayName("should get person employment details successfully")
    fun `getPersonEmploymentDetails should get person employment details successfully`() {
        // Given
        val expectedResponse = createTestEmploymentDetailsResponse(personId = personId)
        every { personReadService.getPersonEmploymentDetails(personId) } returns expectedResponse

        // When
        val result = personService.getPersonEmploymentDetails(personId)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.employmentId, result?.employmentId)
        assertEquals(expectedResponse.personId, result?.personId)
        assertEquals(expectedResponse.employerName, result?.employerName)

        verify(exactly = 1) { personReadService.getPersonEmploymentDetails(personId) }
    }

    @Test
    @DisplayName("should return null when employment details not found")
    fun `getPersonEmploymentDetails should return null when employment details not found`() {
        // Given
        every { personReadService.getPersonEmploymentDetails(personId) } returns null

        // When
        val result = personService.getPersonEmploymentDetails(personId)

        // Then
        assertEquals(null, result)

        verify(exactly = 1) { personReadService.getPersonEmploymentDetails(personId) }
    }

    @Test
    @DisplayName("should create person employment details successfully")
    fun `createPersonEmploymentDetails should create person employment details successfully`() {
        // Given
        val createRequest = createTestEmploymentDetailsCreateRequest()
        val expectedResponse = createTestEmploymentDetailsResponse(personId = personId)
        every { personWriteService.createPersonEmploymentDetails(personId, createRequest) } returns expectedResponse

        // When
        val result = personService.createPersonEmploymentDetails(personId, createRequest)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.employmentId, result.employmentId)
        assertEquals(expectedResponse.personId, result.personId)
        assertEquals(expectedResponse.employerName, result.employerName)

        verify(exactly = 1) { personWriteService.createPersonEmploymentDetails(personId, createRequest) }
    }

    @Test
    @DisplayName("should update person employment details successfully")
    fun `updatePersonEmploymentDetails should update person employment details successfully`() {
        // Given
        val updateRequest = createTestEmploymentDetailsUpdateRequest()
        val expectedResponse = createTestEmploymentDetailsResponse(
            personId = personId,
            employerName = "Updated Company"
        )
        every { personWriteService.updatePersonEmploymentDetails(personId, updateRequest) } returns expectedResponse

        // When
        val result = personService.updatePersonEmploymentDetails(personId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.employmentId, result.employmentId)
        assertEquals(expectedResponse.personId, result.personId)
        assertEquals(expectedResponse.employerName, result.employerName)

        verify(exactly = 1) { personWriteService.updatePersonEmploymentDetails(personId, updateRequest) }
    }

    @Test
    @DisplayName("should delete person employment details successfully")
    fun `deletePersonEmploymentDetails should delete person employment details successfully`() {
        // Given
        every { personWriteService.deletePersonEmploymentDetails(personId) } returns Unit

        // When
        personService.deletePersonEmploymentDetails(personId)

        // Then
        verify(exactly = 1) { personWriteService.deletePersonEmploymentDetails(personId) }
    }
}
