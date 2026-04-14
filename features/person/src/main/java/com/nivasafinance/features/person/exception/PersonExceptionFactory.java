package com.nivasafinance.features.person.exception;

import org.springframework.context.MessageSource;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;

public class PersonExceptionFactory {

    public static PersonNotFoundException notFound(Long id, MessageSource messageSource) {
        return new PersonNotFoundException(id, messageSource);
    }

    public static PersonMobileNumberNotFoundException mobileNumberNotFound(String mobileNo, MessageSource messageSource) {
        return new PersonMobileNumberNotFoundException(mobileNo, messageSource);
    }

    public static PersonOperationException createFailed(MessageSource messageSource) {
        return new PersonOperationException("error.person.operation.create", messageSource);
    }

    public static PersonOperationException updateFailed(MessageSource messageSource) {
        return new PersonOperationException("error.person.operation.update", messageSource);
    }

    public static PersonOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new PersonOperationException("error.person.operation.retrieve", messageSource);
    }

    public static PersonPrimaryMobileAlreadyExistsException primaryMobileAlreadyExists(
            String mobileNumber,
            MessageSource messageSource) {
        return new PersonPrimaryMobileAlreadyExistsException(mobileNumber, messageSource);
    }

    public static BadRequestException addressTypeMandatory(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.person.address.type.mandatory",
                null,
                messageSource
        ));
    }

    public static BadRequestException personAlreadyDeleted(Object identifier, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.person.already.deleted", new Object[]{identifier}, messageSource));
    }

    public static BadRequestException personNotDeleted(Object identifier, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.person.not.deleted", new Object[]{identifier}, messageSource));
    }
}

