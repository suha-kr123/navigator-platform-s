package com.nivasafinance.features.leadstages.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class LeadStageHistoryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String STAGE_HISTORY_NOT_FOUND_MESSAGE_KEY = "lead.stage.history.not.found";
    private static final String NO_STAGE_HISTORY_FOUND_MESSAGE_KEY = "lead.stage.history.no.history.found";
    private static final String NO_ACTIVE_STAGE_FOUND_MESSAGE_KEY = "lead.stage.history.no.active.stage.found";

    private LeadStageHistoryNotFoundException(String message) {
        super(message);
    }

    public static LeadStageHistoryNotFoundException stageHistoryNotFound(Long id) {
        String defaultMessage = String.format("Stage history not found with id %s", id);
        return new LeadStageHistoryNotFoundException(defaultMessage);
    }

    public static LeadStageHistoryNotFoundException stageHistoryNotFound(Long id, MessageSource messageSource) {
        String defaultMessage = String.format("Stage history not found with id %s", id);
        
        String message = messageSource != null
                ? messageSource.getMessage(STAGE_HISTORY_NOT_FOUND_MESSAGE_KEY,
                new Object[]{id}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryNotFoundException(message);
    }

    public static LeadStageHistoryNotFoundException noStageHistoryFound(Long leadId) {
        String defaultMessage = String.format("No stage history found for lead %s", leadId);
        return new LeadStageHistoryNotFoundException(defaultMessage);
    }

    public static LeadStageHistoryNotFoundException noStageHistoryFound(Long leadId, MessageSource messageSource) {
        String defaultMessage = String.format("No stage history found for lead %s", leadId);
        
        String message = messageSource != null
                ? messageSource.getMessage(NO_STAGE_HISTORY_FOUND_MESSAGE_KEY,
                new Object[]{leadId}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryNotFoundException(message);
    }

    public static LeadStageHistoryNotFoundException noActiveStageFound(Long leadId) {
        String defaultMessage = String.format("No active stage found for lead %s", leadId);
        return new LeadStageHistoryNotFoundException(defaultMessage);
    }

    public static LeadStageHistoryNotFoundException noActiveStageFound(Long leadId, MessageSource messageSource) {
        String defaultMessage = String.format("No active stage found for lead %s", leadId);
        
        String message = messageSource != null
                ? messageSource.getMessage(NO_ACTIVE_STAGE_FOUND_MESSAGE_KEY,
                new Object[]{leadId}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryNotFoundException(message);
    }
}

