package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import org.modelmapper.ModelMapper
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.UUID

@Service
class PersonReadServiceImpl(
    private val personRepository: PersonRepository,
    private val modelMapper: ModelMapper,
    private val messageSource: MessageSource
) : PersonReadService { //todo base

    private val locale: Locale = LocaleContextHolder.getLocale()

    companion object {
        private const val CACHE_NAME = "person"
        private const val PERSON_ID_NOT_FOUND_KEY = "error.person.id.not.found"
        private const val PERSON_MOBILE_NOT_FOUND_KEY = "error.person.mobile.not.found"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getPerson(id: UUID): PersonDto {
        val person = personRepository.findById(id).orElseThrow {
            personNotFoundException(id)
        }

        return modelMapper.map(person, PersonDto::class.java)
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