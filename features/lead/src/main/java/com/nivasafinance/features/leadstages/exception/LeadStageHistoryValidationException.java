package com.nivasafinance.features.leadstages.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LeadStageHistoryValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String USER_NOT_IN_ASSIGNEE_ROLES_MESSAGE_KEY = "lead.stage.history.user.not.in.assignee.roles";
    private static final String STAGE_FROM_MISMATCH_MESSAGE_KEY = "lead.stage.history.stage.from.mismatch";
    private static final String PREVIOUS_STAGE_KEY_REQUIRED_WHEN_HISTORY_EXISTS_MESSAGE_KEY = "lead.stage.history.previous.stage.key.required.when.history.exists";
    private static final String PREVIOUS_STAGE_KEY_PROVIDED_FOR_FIRST_ENTRY_MESSAGE_KEY = "lead.stage.history.previous.stage.key.provided.for.first.entry";
    private static final String NO_ACTIVE_STAGE_ENTRY_MESSAGE_KEY = "lead.stage.history.no.active.stage.entry";
    private static final String STAGE_KEY_MISMATCH_MESSAGE_KEY = "lead.stage.history.stage.key.mismatch";
    private static final String STAGE_ALREADY_EXITED_MESSAGE_KEY = "lead.stage.history.stage.already.exited";
    private static final String INVALID_SUB_STAGE_KEY_MESSAGE_KEY = "lead.stage.history.invalid.sub.stage.key";
    private static final String INVALID_STAGE_TRANSITION_MESSAGE_KEY = "lead.stage.history.invalid.stage.transition";

    private LeadStageHistoryValidationException(String message) {
        super(message);
    }

    public static LeadStageHistoryValidationException userNotInAssigneeRoles(String username,
                                                                              List<String> allowedRoles,
                                                                              MessageSource messageSource) {
        List<String> safeRoles = allowedRoles == null ? Collections.emptyList() : allowedRoles;
        String joinedRoles = safeRoles.stream().collect(Collectors.joining(", "));
        String defaultMessage = String.format("User %s is not allowed to be assigned to roles [%s]", username, joinedRoles);

        String message = messageSource != null
                ? messageSource.getMessage(USER_NOT_IN_ASSIGNEE_ROLES_MESSAGE_KEY,
                new Object[]{username, joinedRoles}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException stageFromMismatch(String requestedPreviousStage,
                                                                        String currentStage,
                                                                        MessageSource messageSource) {
        String defaultMessage = String.format("Previous stage %s does not match current stage %s", requestedPreviousStage, currentStage);

        String message = messageSource != null
                ? messageSource.getMessage(STAGE_FROM_MISMATCH_MESSAGE_KEY,
                new Object[]{requestedPreviousStage, currentStage}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException previousStageKeyRequiredWhenHistoryExists(Long leadId,
                                                                                                MessageSource messageSource) {
        String defaultMessage = String.format("Previous stage key is required. History exists for lead %s", leadId);

        String message = messageSource != null
                ? messageSource.getMessage(PREVIOUS_STAGE_KEY_REQUIRED_WHEN_HISTORY_EXISTS_MESSAGE_KEY,
                new Object[]{leadId}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException previousStageKeyProvidedForFirstEntry(String previousStageKey,
                                                                                            MessageSource messageSource) {
        String defaultMessage = String.format("Previous stage key %s should not be provided for the first entry", previousStageKey);

        String message = messageSource != null
                ? messageSource.getMessage(PREVIOUS_STAGE_KEY_PROVIDED_FOR_FIRST_ENTRY_MESSAGE_KEY,
                new Object[]{previousStageKey}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException noActiveStageEntry(Long leadId, MessageSource messageSource) {
        String defaultMessage = String.format("No active stage entry found for lead %s", leadId);

        String message = messageSource != null
                ? messageSource.getMessage(NO_ACTIVE_STAGE_ENTRY_MESSAGE_KEY,
                new Object[]{leadId}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException stageKeyMismatch(String requestedStageKey,
                                                                       String currentStageKey,
                                                                       MessageSource messageSource) {
        String defaultMessage = String.format("Requested stage key %s does not match current stage key %s", requestedStageKey, currentStageKey);

        String message = messageSource != null
                ? messageSource.getMessage(STAGE_KEY_MISMATCH_MESSAGE_KEY,
                new Object[]{requestedStageKey, currentStageKey}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException stageAlreadyExited(String stageKey, MessageSource messageSource) {
        String defaultMessage = String.format("Stage %s has already been exited", stageKey);

        String message = messageSource != null
                ? messageSource.getMessage(STAGE_ALREADY_EXITED_MESSAGE_KEY,
                new Object[]{stageKey}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException invalidSubStageKey(String subStageKey,
                                                                         String stageKey,
                                                                         MessageSource messageSource) {
        String defaultMessage = String.format("Sub stage key %s is not valid for stage %s", subStageKey, stageKey);

        String message = messageSource != null
                ? messageSource.getMessage(INVALID_SUB_STAGE_KEY_MESSAGE_KEY,
                new Object[]{subStageKey, stageKey}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }

    public static LeadStageHistoryValidationException invalidStageTransition(String fromStageKey,
                                                                             String toStageKey,
                                                                             MessageSource messageSource) {
        String defaultMessage = String.format("Invalid stage transition from %s to %s", fromStageKey, toStageKey);

        String message = messageSource != null
                ? messageSource.getMessage(INVALID_STAGE_TRANSITION_MESSAGE_KEY,
                new Object[]{fromStageKey, toStageKey}, defaultMessage, LocaleContextHolder.getLocale())
                : defaultMessage;

        return new LeadStageHistoryValidationException(message);
    }
}
