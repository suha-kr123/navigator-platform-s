package com.nivasafinance.features.admin.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public final class AdminExceptionFactory {

    private AdminExceptionFactory() {
    }

    public static BadRequestException cannotRestoreUserPersonDeleted(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.admin.user.restore.person.deleted", new Object[]{}, messageSource));
    }

    public static BadRequestException cannotRestoreStaffUserDeleted(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.admin.staff.restore.user.deleted", new Object[]{}, messageSource));
    }

    public static BadRequestException cannotRestoreAdvisorUserDeleted(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.admin.advisor.restore.user.deleted", new Object[]{}, messageSource));
    }

    public static BadRequestException cannotRestoreLeadPersonDeleted(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.admin.lead.restore.person.deleted", new Object[]{}, messageSource));
    }
}
