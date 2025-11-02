package com.nivasafinance.features.person.repository;

import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.exception.PersonExceptionFactory;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PersonRepositoryWrapper {

    private final PersonRepository personRepository;
    private final MessageSource messageSource;

    public Person saveWithException(Person person) {
        try {
            return personRepository.save(person);
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.createFailed(messageSource);
        }
    }

    public Person findByIdWithException(Long id) {
        try {
            return personRepository.findById(id)
                    .orElseThrow(() -> PersonExceptionFactory.notFound(id, messageSource));
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Optional<Person> findByPrimaryMobileNumber(String mobileNumber) {
        try {
            return personRepository.findByPrimaryMobileNumber(mobileNumber);
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Person findByPrimaryMobileNumberWithException(String mobileNumber) {
        try {
            return personRepository.findByPrimaryMobileNumber(mobileNumber).orElseThrow(() ->
                    PersonExceptionFactory.mobileNumberNotFound(mobileNumber, messageSource));
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }
}

