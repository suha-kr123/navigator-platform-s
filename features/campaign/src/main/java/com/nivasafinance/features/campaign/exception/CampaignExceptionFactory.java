package com.nivasafinance.features.campaign.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.UUID;

public final class CampaignExceptionFactory {

    private CampaignExceptionFactory() {
        // Utility class
    }

    public static CampaignOperationException saveFailed(MessageSource messageSource) {
        return new CampaignOperationException("error.campaign.save.failed", null, messageSource);
    }

    public static CampaignOperationException retrieveByIdFailed(Long id, MessageSource messageSource) {
        return new CampaignOperationException("error.campaign.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static CampaignOperationException retrieveByIdentifierFailed(UUID identifier, MessageSource messageSource) {
        return new CampaignOperationException("error.campaign.retrieve.by.identifier.failed", new Object[]{identifier}, messageSource);
    }

    public static CampaignOperationException retrieveByIdsFailed(List<Long> ids, MessageSource messageSource) {
        return new CampaignOperationException(
                "error.campaign.retrieve.by.ids.failed",
                new Object[]{ids == null ? null : ids.toString()},
                messageSource
        );
    }

    public static CampaignNotFoundException notFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.campaign.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new CampaignNotFoundException(message);
    }

    public static CampaignNotFoundException notFoundByIdentifier(UUID identifier, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.campaign.not.found.by.identifier",
                new Object[]{identifier},
                messageSource
        );
        return new CampaignNotFoundException(message);
    }

    public static CampaignValidationException duplicateName(String name, MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.name.already.exists",
                new Object[]{name},
                messageSource
        );
    }

    public static CampaignValidationException configNotActive(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.config.not.active",
                null,
                messageSource
        );
    }

    public static CampaignValidationException unsupportedCampaignType(String campaignType, MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.unsupported.type",
                new Object[]{campaignType},
                messageSource
        );
    }

    public static CampaignValidationException voiceConfigsRequired(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.voice.configs.required",
                null,
                messageSource
        );
    }

    public static CampaignValidationException configsRequired(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.configs.required",
                null,
                messageSource
        );
    }

    public static CampaignValidationException campaignNotDraft(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.not.draft",
                null,
                messageSource
        );
    }

    public static CampaignValidationException documentNotUploaded(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.document.not.uploaded",
                null,
                messageSource
        );
    }

    public static CampaignValidationException documentNotGenerated(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.document.not.generated",
                null,
                messageSource
        );
    }

    public static CampaignValidationException campaignNotSubmitted(MessageSource messageSource) {
        return new CampaignValidationException(
                "error.campaign.not.refresh",
                null,
                messageSource
        );
    }
}

