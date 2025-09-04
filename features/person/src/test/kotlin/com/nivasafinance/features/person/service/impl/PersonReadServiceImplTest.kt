package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsEntity
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.entity.EmploymentDetails
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.EmploymentDetailsRepository
import com.nivasafinance.features.person.repository.PersonAddressMappingRepository
import com.nivasafinance.features.person.repository.PersonIdentifierRepository
import com.nivasafinance.features.person.repository.PersonRepository
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

@DisplayName("PersonReadServiceImpl Tests")
class PersonReadServiceImplTest {

    private val personRepository = mockk<PersonRepository>()
    private val personAddressMappingRepository = mockk<PersonAddressMappingRepository>()
    private val personIdentifierRepository = mockk<PersonIdentifierRepository>()
    private val employmentDetailsRepository = mockk<EmploymentDetailsRepository>()
    private val addressService = mockk<AddressService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personReadService: PersonReadServiceImpl

    private val personId = UUID.randomUUID()
    private val person = com.nivasafinance.features.person.entity.Person(
        id = personId,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        gender = data.enums.Gender.MALE
    )
    private val expectedData = PersonData.fromEntity(person)

    @BeforeEach
    fun setup() {
        personReadService = PersonReadServiceImpl(
            personRepository,
            personAddressMappingRepository,
            personIdentifierRepository,
            employmentDetailsRepository,
            addressService
        )

        // Mock modelMapper for BaseNavigatorService using reflection
        val modelMapperField = personReadService.javaClass.superclass.getDeclaredField("modelMapper")
        modelMapperField.isAccessible = true
        modelMapperField.set(personReadService, modelMapper)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = personReadService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(personReadService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
    }

    @Test
    @DisplayName("should return person when found")
    fun `getPerson should return person when found`() {
        // Given
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When
        val result = personReadService.getPerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(expectedData.id, result.id)
        assertEquals(expectedData.firstName, result.firstName)
        assertEquals(expectedData.lastName, result.lastName)

        verify(exactly = 1) { personRepository.findById(personId) }
    }

    @Test
    @DisplayName("should throw exception when not found")
    fun `getPerson should throw exception when not found`() {
        // Given
        every { personRepository.findById(personId) } returns Optional.empty()

        // When & Then
        assertThrows<PersonNotFoundException> {
            personReadService.getPerson(personId)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
    }

    // Employment Details Tests

    @Test
    @DisplayName("should get person employment details successfully")
    fun `getPersonEmploymentDetails should get person employment details successfully`() {
        // Given
        val employmentId = UUID.randomUUID()
        val employmentDetails = createTestEmploymentDetailsEntity(
            employmentId = employmentId,
            personId = personId
        )
        
        every { personRepository.existsById(personId) } returns true
        every { employmentDetailsRepository.findByPersonId(personId) } returns employmentDetails

        // When
        val result = personReadService.getPersonEmploymentDetails(personId)

        // Then
        assertNotNull(result)
        assertEquals(employmentId, result?.employmentId)
        assertEquals(personId, result?.personId)
        assertEquals(employmentDetails.employerName, result?.employerName)
        assertEquals(employmentDetails.employerType, result?.employerType)
        assertEquals(employmentDetails.jobTitle, result?.jobTitle)
        assertEquals(employmentDetails.department, result?.department)
        assertEquals(employmentDetails.employmentType, result?.employmentType)
        assertEquals(employmentDetails.location, result?.location)
        assertEquals(employmentDetails.salary, result?.salary)
        assertEquals(employmentDetails.documents, result?.documents)
        assertEquals(employmentDetails.extData, result?.extData)

        verify(exactly = 1) { personRepository.existsById(personId) }
        verify(exactly = 1) { employmentDetailsRepository.findByPersonId(personId) }
    }

    @Test
    @DisplayName("should return null when employment details not found")
    fun `getPersonEmploymentDetails should return null when employment details not found`() {
        // Given
        every { personRepository.existsById(personId) } returns true
        every { employmentDetailsRepository.findByPersonId(personId) } returns null

        // When
        val result = personReadService.getPersonEmploymentDetails(personId)

        // Then
        assertEquals(null, result)

        verify(exactly = 1) { personRepository.existsById(personId) }
        verify(exactly = 1) { employmentDetailsRepository.findByPersonId(personId) }
    }

    @Test
    @DisplayName("should throw PersonNotFoundException when person does not exist")
    fun `getPersonEmploymentDetails should throw PersonNotFoundException when person does not exist`() {
        // Given
        every { personRepository.existsById(personId) } returns false

        // When & Then
        assertThrows<PersonNotFoundException> {
            personReadService.getPersonEmploymentDetails(personId)
        }

        verify(exactly = 1) { personRepository.existsById(personId) }
        verify(exactly = 0) { employmentDetailsRepository.findByPersonId(personId) }
    }
}
