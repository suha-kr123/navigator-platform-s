package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogResponse;
import com.nivasafinance.features.lead.dto.CreateLeadCallRequest;
import com.nivasafinance.features.lead.dto.CreateLeadCallResponse;
import com.nivasafinance.features.lead.dto.LeadUpdateCallLog;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadCallWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadCallWriteServiceImpl implements LeadCallWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final PersonReadService personReadService;
    private final CallWriteService callWriteService;
    private final UserReadService userReadService;

    @Override
    public CreateLeadCallResponse callContact(UUID leadIdentifier, CreateLeadCallRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(request.getContactIdentifier());
        PersonResponse person = personReadService.getPersonById(contact.getPersonId());

        validateContactBelongsToLead(lead, contact.getId());
        validatePhoneBelongsToPerson(person, request.getPhoneNumber());


        InitiateCallRequest initiateCallRequest = InitiateCallRequest.builder()
                .fromPhoneNumber(getCurrentUserPrimaryNumber())
                .toPhoneNumber(request.getPhoneNumber())
                .entity(SystemEntities.LEAD)
                .entityId(lead.getId())
                .identifier(leadIdentifier.toString())
                .businessPurpose("Call for person : " + person.getDisplayName())
                .build();

        InitiateCallResponse response = callWriteService.call(initiateCallRequest);
        List<Lead.CallLogDetails> callLogs = lead.getCallLogDetails();
        if (callLogs == null) {
            callLogs = new java.util.ArrayList<>();
        }
        callLogs.add(new Lead.CallLogDetails(response.getId(), response.getIdentifier()));
        lead.setCallLogDetails(callLogs);
        return new CreateLeadCallResponse(response.getIdentifier(), response.getStatus());
    }

    @Override
    public void updateCallLog(UUID leadIdentifier, String externalId, LeadUpdateCallLog request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
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
                for (LeadUpdateCallLog.CompletionLeg leadLeg : request.getCompletionDetails().getLegs()) {
                    legs.add(CallLog.CompletionLeg.builder().duration(leadLeg.getDuration()).status(leadLeg.getStatus()).build());
                }
                completionDetails.setLegs(legs);
            }
        }
        callWriteService.updateCallLogByProviderId(externalId, updateCallLog);
    }

    @Override
    @Transactional
    public CreateExternalCallLogResponse createExternalCallLog(UUID leadIdentifier, CreateExternalCallLogRequest request) {
        // Validate lead exists
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

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

        // Link call log to lead
        List<Lead.CallLogDetails> callLogs = lead.getCallLogDetails();
        if (callLogs == null) {
            callLogs = new ArrayList<>();
        }
        callLogs.add(new Lead.CallLogDetails(savedCallLog.getId(), savedCallLog.getIdentifier()));
        lead.setCallLogDetails(callLogs);
        leadRepositoryWrapper.saveWithException(lead);

        // Return response
        return CreateExternalCallLogResponse.builder()
                .identifier(savedCallLog.getIdentifier())
                .status(savedCallLog.getStatus())
                .build();
    }

    private void validateContactBelongsToLead(Lead lead, Long contactId) {
        List<Long> contacts = lead.getContacts();
        if (CollectionUtils.isEmpty(contacts) || !contacts.contains(contactId)) {
            throw new BadRequestException("Contact does not belong to the provided lead identifier");
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
            throw new BadRequestException("Provided phone number does not belong to the contact");
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
}

