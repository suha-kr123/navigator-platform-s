package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PersonWriteServiceImpl(
    private val personRepository: PersonRepository,
) : PersonWriteService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "person"
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun savePerson(personDto: PersonDto): PersonDto {
        val personEntity = modelMapper.map(personDto, Person::class.java)
        val savedPersonEntity = personRepository.save(personEntity)
        val savedDto = modelMapper.map(savedPersonEntity, PersonDto::class.java)
        return savedDto
    }

    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    @Transactional
    override fun deletePerson(id: UUID) {
        if (!personRepository.existsById(id)) {
            throw PersonNotFoundException(id, messageSource)
        }
        personRepository.deleteById(id)
    }
}
