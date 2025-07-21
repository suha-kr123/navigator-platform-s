package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PersonWriteServiceImpl(
    private val personRepository: PersonRepository,
) : PersonWriteService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "person"
        private const val PERSON_ID_NOT_FOUND_KEY = "error.person.id.not.found"
        private val locale = LocaleContextHolder.getLocale()
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun savePerson(personDto: PersonDto): PersonDto {
        val person = modelMapper.map(personDto, Person::class.java)
        val savedPerson = personRepository.save(person)
        return modelMapper.map(savedPerson, PersonDto::class.java)
    }


    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    @Transactional
    override fun deletePerson(id: UUID) {
        if (personRepository.existsById(id)) {
            personNotFoundException(id)
        }
        personRepository.deleteById(id)
    }

    private fun personNotFoundException(id: UUID): RuntimeException {
        return RuntimeException(
            messageSource.getMessage(
                PERSON_ID_NOT_FOUND_KEY,
                arrayOf(id),
                locale
            )
        )
    }
}