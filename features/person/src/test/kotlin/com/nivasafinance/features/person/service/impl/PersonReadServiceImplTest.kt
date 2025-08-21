package com.nivasafinance.features.person.service.impl

import com.nivasafinance.TestUtils.createTestPerson
import com.nivasafinance.TestUtils.createTestPersonDto
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.exception.PersonNotFoundException
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
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personReadService: PersonReadServiceImpl

    private val personId = UUID.randomUUID()
    private val person = createTestPerson(id = personId)
    private val expectedDto = createTestPersonDto(id = personId)

    @BeforeEach
    fun setup() {
        personReadService = PersonReadServiceImpl(personRepository).apply {
            this.modelMapper = this@PersonReadServiceImplTest.modelMapper
            this.messageSource = this@PersonReadServiceImplTest.messageSource
        }
    }

    @Test
    @DisplayName("should return person when found")
    fun `getPerson should return person when found`() {
        // Given
        every { personRepository.findById(personId) } returns Optional.of(person)
        every { modelMapper.map(person, PersonDto::class.java) } returns expectedDto

        // When
        val result = personReadService.getPerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.id, result.id)
        assertEquals(expectedDto.firstName, result.firstName)
        assertEquals(expectedDto.lastName, result.lastName)

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { modelMapper.map(person, PersonDto::class.java) }
    }

    @Test
    @DisplayName("should throw exception when not found")
    fun `getPerson should throw exception when not found`() {
        // Given
        every { personRepository.findById(personId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"

        // When & Then
        assertThrows<PersonNotFoundException> {
            personReadService.getPerson(personId)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }
}
