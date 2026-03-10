package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.AdvisorCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.AdvisorCallLogUpdateEventPayload;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.advisor.dto.CreateAdvisorCallRequest;
import com.nivasafinance.features.advisor.dto.CreateAdvisorCallResponse;
import com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.advisor.dto.CreateExternalCallLogResponse;
import com.nivasafinance.features.advisor.dto.AdvisorUpdateCallLog;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorCallWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdvisorCallWriteServiceImpl implements AdvisorCallWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final CallWriteService callWriteService;
    private final CallReadService callReadService;
    private final UserReadService userReadService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public CreateAdvisorCallResponse callPerson(UUID advisorIdentifier, CreateAdvisorCallRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        PersonResponse person = userReadService.getPersonForUser(advisor.getUsername());

        validatePhoneBelongsToPerson(person, request.getPhoneNumber());

        InitiateCallRequest initiateCallRequest = InitiateCallRequest.builder()
                .fromPhoneNumber(getCurrentUserPrimaryNumber())
                .toPhoneNumber(request.getPhoneNumber())
                .entity(SystemEntities.ADVISOR)
                .entityId(advisor.getId())
                .identifier(advisorIdentifier.toString())
                .businessPurpose("Call for advisor : " + person.getDisplayName())
                .build();

        InitiateCallResponse response = callWriteService.call(initiateCallRequest);
        List<Advisor.CallLogDetails> callLogs = advisor.getCallLogDetails();
        if (callLogs == null) {
            callLogs = new ArrayList<>();
        }
        callLogs.add(new Advisor.CallLogDetails(response.getId()));
        advisor.setCallLogDetails(callLogs);

        // Update lastCallId based on latest createdAt
        updateLastCallId(advisor, response.getId());

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish event
        publishAdvisorCallLogCreatedEvent(advisor, response.getId(), response.getIdentifier());

        return new CreateAdvisorCallResponse(response.getIdentifier(), response.getStatus());
    }

    @Override
    public void updateCallLog(UUID advisorIdentifier, String externalId, AdvisorUpdateCallLog request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        UpdateCallLog updateCallLog = new UpdateCallLog();
        updateCallLog.setStatus(request.getStatus());
        if (request.getRecordingDetails() != null) {
            updateCallLog.setRecordingDetails(
                    CallLog.RecordingDetails.builder().url(request.getRecordingDetails().getUrl()).build()
            );
        }
        if (request.getCompletionDetails() != null) {
            CallLog.CompletionDetails completionDetails = CallLog.CompletionDetails.builder()
                    .duration(request.getCompletionDetails().getDuration())
                    .startTime(request.getCompletionDetails().getStartTime())
                    .endTime(request.getCompletionDetails().getEndTime())
                    .build();
            if (request.getCompletionDetails().getLegs() != null) {
                List<CallLog.CompletionLeg> legs = new ArrayList<>();
                for (AdvisorUpdateCallLog.CompletionLeg advisorLeg : request.getCompletionDetails().getLegs()) {
                    legs.add(CallLog.CompletionLeg.builder().duration(advisorLeg.getDuration()).direction(advisorLeg.getDirection()).status(advisorLeg.getStatus()).build());
                }
                completionDetails.setLegs(legs);
            }
            updateCallLog.setCompletionDetails(completionDetails);
        }
        callWriteService.updateCallLogByProviderId(externalId, updateCallLog);

        CallLogResponse updatedCallLog = callReadService.getCallLogByProviderId(externalId)
                .orElseThrow(() -> new BadRequestException("Call Log with povider ID" + externalId + " not found"));

        publishAdvisorCallLogUpdatedEvent(advisor, updatedCallLog.getId(), updatedCallLog.getIdentifier());
    }

    @Override
    @Transactional
    public CreateExternalCallLogResponse createExternalCallLog(UUID advisorIdentifier, CreateExternalCallLogRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        PersonResponse person = userReadService.getPersonForUser(advisor.getUsername());

        validatePhoneBelongsToPerson(person, request.getToNumber());

        // Create new CallLog entity
        CallLog callLog = new CallLog();
        callLog.setProvider(request.getProvider());
        callLog.setProviderId(request.getProviderId());
        callLog.setCallerId(request.getCallerId());
        callLog.setFromNumber(request.getFromNumber());
        callLog.setToNumber(request.getToNumber());
        callLog.setDirection(request.getDirection());
        callLog.setSource(CallSource.API);
        callLog.setStatus(request.getStatus());
        if (request.getCreatedAt() != null) {
            callLog.setCreatedAt(request.getCreatedAt());
        }
        callLog.setRecordingDetails(request.getRecordingDetails());
        callLog.setCompletionDetails(request.getCompletionDetails());

        // Save call log (duplicate check is done in CallWriteService.createCallLog)
        CreateCallLogResponse savedCallLog = callWriteService.createCallLog(callLog);

        // Link call log to advisor
        List<Advisor.CallLogDetails> callLogs = advisor.getCallLogDetails();
        if (callLogs == null) {
            callLogs = new ArrayList<>();
        }
        callLogs.add(new Advisor.CallLogDetails(savedCallLog.getId()));
        advisor.setCallLogDetails(callLogs);

        // Update lastCallId based on latest createdAt
        updateLastCallId(advisor, savedCallLog.getId());

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish event
        publishAdvisorCallLogCreatedEvent(advisor, savedCallLog.getId(), savedCallLog.getIdentifier());

        // Return response
        return CreateExternalCallLogResponse.builder()
                .identifier(savedCallLog.getIdentifier())
                .status(savedCallLog.getStatus())
                .build();
    }

    private void updateLastCallId(Advisor advisor, Long newCallLogId) {
        com.nivasafinance.features.advisor.dto.OtherDetails otherDetails = advisor.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new com.nivasafinance.features.advisor.dto.OtherDetails();
        }

        Long currentLastCallId = otherDetails.getLastCallId();

        // If current is null, automatically use new
        if (currentLastCallId == null) {
            otherDetails.setLastCallId(newCallLogId);
            advisor.setOtherDetails(otherDetails);
            return;
        }

        // If current and new are the same, no need to update
        if (currentLastCallId.equals(newCallLogId)) {
            return;
        }

        // Get both call logs to compare createdAt
        CallLogResponse currentCallLog = callReadService.getCallLogByID(currentLastCallId);
        CallLogResponse newCallLog = callReadService.getCallLogByID(newCallLogId);

        // If either is null or doesn't have createdAt, use new one
        if (currentCallLog == null || currentCallLog.getCreatedAt() == null ||
                newCallLog == null || newCallLog.getCreatedAt() == null) {
            otherDetails.setLastCallId(newCallLogId);
            advisor.setOtherDetails(otherDetails);
            return;
        }

        // Compare createdAt dates - use the one with later date
        if (newCallLog.getCreatedAt().isAfter(currentCallLog.getCreatedAt())) {
            otherDetails.setLastCallId(newCallLogId);
            advisor.setOtherDetails(otherDetails);
        }
    }

    private void validatePhoneBelongsToPerson(PersonResponse person, String phoneNumber) {
        boolean matches = person != null &&
                person.getMobileNumbers() != null &&
                person.getMobileNumbers().stream()
                        .filter(Objects::nonNull)
                        .map(MobileNumberDetails::getNumber)
                        .anyMatch(number -> Objects.equals(number, phoneNumber));
        if (!matches) {
            throw new BadRequestException("Provided phone number does not belong to the advisor");
        }
    }

    private String getCurrentUserPrimaryNumber() {
        UserResponse currentUser = userReadService.getUserByUsername(UserContext.getUsername());
        return currentUser.getPersonResponse()
                .getMobileNumbers()
                .stream()
                .filter(MobileNumberDetails::getIsPrimary)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Current User Does not have primary mobile number"))
                .getNumber();
    }

    private void publishAdvisorCallLogCreatedEvent(Advisor advisor, Long callLogId, UUID callLogIdentifier) {
        AdvisorCallLogCreationEventPayload payload = AdvisorCallLogCreationEventPayload.builder()
                .advisorId(advisor.getId())
                .callLogId(callLogId)
                .callLogIdentifier(callLogIdentifier)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_CALL_LOG_CREATED.toString(), payload, username)
        );
    }

    private void publishAdvisorCallLogUpdatedEvent(Advisor advisor, Long callLogId, UUID callLogIdentifier) {
        AdvisorCallLogUpdateEventPayload.builder()
                .advisorId(advisor.getId())
                .callLogId(callLogId)
                .callLogIdentifier(callLogIdentifier)
                .build();
    }
}
