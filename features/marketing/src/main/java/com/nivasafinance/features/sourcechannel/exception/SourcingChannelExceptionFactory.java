package com.nivasafinance.features.sourcechannel.exception;

import org.springframework.context.MessageSource;

public class SourcingChannelExceptionFactory {

    private SourcingChannelExceptionFactory() {
        // Private constructor to prevent instantiation
    }

    public static SourcingChannelOperationException createFailed(MessageSource messageSource) {
        return new SourcingChannelOperationException("error.sourcingchannel.operation.create", messageSource);
    }

    public static SourcingChannelOperationException updateFailed(MessageSource messageSource) {
        return new SourcingChannelOperationException("error.sourcingchannel.operation.update", messageSource);
    }

    public static SourcingChannelNotFoundException notFound(Long id, MessageSource messageSource) {
        return new SourcingChannelNotFoundException(id, messageSource);
    }

    public static SourcingChannelNotFoundException notFound(String sourcingIdentifier, MessageSource messageSource) {
        return new SourcingChannelNotFoundException(sourcingIdentifier, messageSource);
    }
}

