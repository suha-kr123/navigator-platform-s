package com.nivasafinance.features.person.repository

import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonExceptionFactory
import com.nivasafinance.features.person.exception.PersonNotFoundException
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PersonRepositoryWrapper(
    private val personRepository: PersonRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(person: Person): Person {
        return try {
            personRepository.save(person)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = PersonExceptionFactory.createFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findByIdWithException(id: UUID): Person {
        return try {
            personRepository.findById(id).orElseThrow {
                PersonExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: PersonNotFoundException) {
            throw e
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = PersonExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findAllWithException(pageable: Pageable): Page<Person> {
        return try {
            personRepository.findAll(pageable)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = PersonExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findByMobileNumberWithException(mobileNumber: String): List<Person> {
        return try {
            personRepository.findByMobileNumber(mobileNumber)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = PersonExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findByMobileNumberExcludingIdWithException(mobileNumber: String, excludeId: UUID): List<Person> {
        return try {
            personRepository.findByMobileNumberExcludingId(mobileNumber, excludeId)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = PersonExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }
}
