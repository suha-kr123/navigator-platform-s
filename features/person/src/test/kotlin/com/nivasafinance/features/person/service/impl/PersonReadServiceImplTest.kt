package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
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

class PersonReadServiceImplTest {

    private val personRepository = mockk<PersonRepository>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personReadService: PersonReadServiceImpl

    private val personId = UUID.randomUUID()
    private val person = Person(id = personId, firstName = "John", lastName = "Doe")
    private val expectedDto = PersonDto(id = personId, firstName = "John", lastName = "Doe")

    @BeforeEach
    fun setup() {
        personReadService = PersonReadServiceImpl(personRepository).apply {
            this.modelMapper = this@PersonReadServiceImplTest.modelMapper
            this.messageSource = this@PersonReadServiceImplTest.messageSource
        }
    }

    @Test
    fun `getPerson should return person when found`() {
        every { personRepository.findById(personId) } returns Optional.of(person)
        every { modelMapper.map(person, PersonDto::class.java) } returns expectedDto

        val result = personReadService.getPerson(personId)

        assertEquals(expectedDto.id, result.id)
        assertEquals(expectedDto.firstName, result.firstName)
        assertEquals(expectedDto.lastName, result.lastName)

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { modelMapper.map(person, PersonDto::class.java) }
    }

    @Test
    fun `getPerson should throw exception when not found`() {
        every { personRepository.findById(personId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"

        assertThrows<PersonNotFoundException> {
            personReadService.getPerson(personId)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }
}
