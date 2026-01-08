package com.nivasafinance.features.campaign.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.UUID;

public final class CampaignConfigExceptionFactory {

    private CampaignConfigExceptionFactory() {
        // Utility class
    }

    public static CampaignConfigOperationException saveFailed(MessageSource messageSource) {
        return new CampaignConfigOperationException("error.campaign.config.save.failed", null, messageSource);
    }

    public static CampaignConfigOperationException retrieveByIdFailed(Long id, MessageSource messageSource) {
        return new CampaignConfigOperationException("error.campaign.config.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static CampaignConfigOperationException retrieveByIdentifierFailed(UUID identifier, MessageSource messageSource) {
        return new CampaignConfigOperationException("error.campaign.config.retrieve.by.identifier.failed", new Object[]{identifier}, messageSource);
    }

    public static CampaignConfigOperationException retrieveByIdsFailed(List<Long> ids, MessageSource messageSource) {
        return new CampaignConfigOperationException(
                "error.campaign.config.retrieve.by.ids.failed",
                new Object[]{ids == null ? null : ids.toString()},
                messageSource
        );
    }

    public static CampaignConfigNotFoundException notFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.campaign.config.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new CampaignConfigNotFoundException(message);
    }

    public static CampaignConfigNotFoundException notFoundByIdentifier(UUID identifier, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.campaign.config.not.found.by.identifier",
                new Object[]{identifier},
                messageSource
        );
        return new CampaignConfigNotFoundException(message);
    }
}

