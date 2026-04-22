package com.nivasafinance.features.leadqueues.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ForbiddenException;
import com.nivasafinance.common.exception.ResourceConflictException;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;

@UtilityClass
public class LeadQueueExceptionFactory {

    public static ResourceNotFoundException leadNotInQueue(String queueName, MessageSource messageSource) {
        return new ResourceNotFoundException(
                ExceptionUtils.createLocalizedMessage(
                        "error.lead.queue.lead.not.in.queue", new Object[]{queueName}, messageSource));
    }

    public static ResourceNotFoundException noClaimableWork(String queueName, MessageSource messageSource) {
        return new ResourceNotFoundException(
                ExceptionUtils.createLocalizedMessage(
                        "error.lead.queue.no.claimable.lead", new Object[]{queueName}, messageSource));
    }

    public static ResourceNotFoundException noQueueEntryForLead(MessageSource messageSource) {
        return new ResourceNotFoundException(
                ExceptionUtils.createLocalizedMessage("error.lead.queue.not.found.for.lead", null, messageSource));
    }

    public static BadRequestException notClaimed(MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage("error.lead.queue.not.claimed", null, messageSource));
    }

    public static ResourceConflictException releaseNotHolder(MessageSource messageSource) {
        return new ResourceConflictException(
                ExceptionUtils.createLocalizedMessage("error.lead.queue.release.wrong.holder", null, messageSource));
    }

    public static BadRequestException heartbeatNotClaimed(MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage("error.lead.queue.heartbeat.not.claimed", null, messageSource));
    }

    public static ResourceConflictException heartbeatNotHolder(MessageSource messageSource) {
        return new ResourceConflictException(
                ExceptionUtils.createLocalizedMessage("error.lead.queue.heartbeat.wrong.holder", null, messageSource));
    }

    public static ForbiddenException queueAccessDenied(MessageSource messageSource) {
        return new ForbiddenException(
                ExceptionUtils.createLocalizedMessage("error.lead.queue.access.denied", null, messageSource));
    }

    /**
     * Queue config JSON must include a non-null data provider with a non-blank dataProviderName.
     */
    public static BadRequestException invalidQueueDataProviderConfig(String queueName, MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage(
                        "error.lead.queue.invalid.data.provider.config", new Object[]{queueName}, messageSource));
    }

    /**
     * n_data_provider has no row for the configured name (misconfiguration), or client passed wrong name.
     */
    public static BadRequestException queueDataProviderNotRegistered(
            String providerName, String queueName, MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage(
                        "error.lead.queue.data.provider.not.registered",
                        new Object[]{providerName, queueName},
                        messageSource));
    }

    /**
     * A second claim in the same queue is not allowed; release the current lead first.
     */
    public static BadRequestException mustReleaseActiveClaimFirst(String queueName, MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage(
                        "error.lead.queue.must.release.active.claim", new Object[]{queueName}, messageSource));
    }
}
