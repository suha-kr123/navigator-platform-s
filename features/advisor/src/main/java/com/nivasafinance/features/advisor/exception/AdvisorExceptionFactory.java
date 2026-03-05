package com.nivasafinance.features.advisor.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.UUID;

public final class AdvisorExceptionFactory {

    private AdvisorExceptionFactory() {
    }

    public static AdvisorNotFoundException notFound(UUID id, MessageSource messageSource) {
        return new AdvisorNotFoundException(id, messageSource);
    }

    public static AdvisorOperationException createFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.create", messageSource);
    }

    public static AdvisorOperationException updateFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.update", messageSource);
    }

    public static AdvisorOperationException deleteFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.delete", messageSource);
    }

    public static AdvisorOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.retrieve", messageSource);
    }

    public static BadRequestException personAlreadyExists(Long personId, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.person.already.exists",
                new Object[]{personId},
                messageSource
        ));
    }

    public static BadRequestException advisorAlreadyExistsForMobileNumber(String mobileNumber, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.mobile.already.exists",
                new Object[]{mobileNumber},
                messageSource
        ));
    }

    public static AdvisorOperationException noCurrentUser(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.no.current.user", messageSource);
    }

    public static AdvisorOperationException notFoundForCurrentUser(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.not.found.for.current.user", messageSource);
    }

    public static BadRequestException notFoundByReferralTrackingCode(String referralTrackingCode, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.not.found.by.referral.tracking.code",
                new Object[]{referralTrackingCode},
                messageSource
        ));
    }

    public static BadRequestException bankDetailsRequiredFieldsMissing(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.bank.details.required.fields",
                new Object[0],
                messageSource
        ));
    }

    public static BadRequestException personalDetailsMobileNumbersRequired(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.self.personalDetails.mobileNumbers.required",
                new Object[0],
                messageSource
        ));
    }

    public static BadRequestException addressRequiredFieldsCannotBeCleared(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.address.required.fields",
                new Object[0],
                messageSource
        ));
    }

    public static BadRequestException advisorReferralCodeNotAvailable(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.referral.code.not.available",
                new Object[0],
                messageSource
        ));
    }

    public static BadRequestException badRequest(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.bad.request",
                new Object[0],
                messageSource
        ));
    }

    public static BadRequestException selfLeadNotAccessible(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.self.lead.not.accessible",
                new Object[0],
                messageSource
        ));
    }
}
