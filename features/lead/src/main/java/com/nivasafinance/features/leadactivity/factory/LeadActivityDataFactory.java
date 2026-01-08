package com.nivasafinance.features.leadactivity.factory;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.payload.*;
import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityRequest;
import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import com.nivasafinance.features.leadactivity.service.LeadActivityWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class LeadActivityDataFactory {
    private final LeadActivityWriteService writeService;

    public <T> void recordEvent(String eventType, T payload, String username) {
        BusinessEvent event = BusinessEvent.valueOf(eventType);
        String effectiveUsername = username != null ? username : UserContext.getUsername();

        switch (event){
            case LEAD_CREATED -> {
                CreateLeadActivityRequest request = createLeadCreatedActivityRequest((LeadCreationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_UPDATED -> {
                CreateLeadActivityRequest request = createLeadUpdatedActivityRequest((LeadUpdateEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_NOTE_CREATED -> {
                CreateLeadActivityRequest request = createLeadNoteCreatedActivityRequest((LeadNoteCreationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_NOTE_UPDATED -> {
                CreateLeadActivityRequest request = createLeadNoteUpdatedActivityRequest((LeadNoteUpdationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_NOTE_DELETED -> {
                CreateLeadActivityRequest request = createLeadNoteDeletedActivityRequest((LeadNoteDeletionEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_DOCUMENT_CREATED -> {
                CreateLeadActivityRequest request = createLeadDocumentCreatedActivityRequest((LeadDocumentCreationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_DOCUMENT_UPDATED -> {
                CreateLeadActivityRequest request = createLeadDocumentUpdatedActivityRequest((LeadDocumentUpdationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_DOCUMENT_DELETED -> {
                CreateLeadActivityRequest request = createLeadDocumentDeletedActivityRequest((LeadDocumentDeletionEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_CONTACT_CREATED -> {
                CreateLeadActivityRequest request = createLeadContactCreatedActivityRequest((LeadContactCreationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_CONTACT_UPDATED -> {
                CreateLeadActivityRequest request = createLeadContactUpdatedActivityRequest((LeadContactUpdationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_CONTACT_DELETED -> {
                CreateLeadActivityRequest request = createLeadContactDeletedActivityRequest((LeadContactDeletionEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_CALL_LOG_CREATED -> {
                CreateLeadActivityRequest request = createLeadCallLogCreatedActivityRequest((LeadCallLogCreationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_CALL_LOG_UPDATED -> {
                CreateLeadActivityRequest request = createLeadCallLogUpdatedActivityRequest((LeadCallLogUpdateEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_LENDER_CREATED -> {
                CreateLeadActivityRequest request = createLeadLenderCreatedActivityRequest((LeadLenderCreationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_LENDER_UPDATED -> {
                CreateLeadActivityRequest request = createLeadLenderUpdatedActivityRequest((LeadLenderUpdationEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_LENDER_REJECTED -> {
                CreateLeadActivityRequest request = createLeadLenderRejectedActivityRequest((LeadLenderRejectionEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_LENDER_SUBMITTED -> {
                CreateLeadActivityRequest request = createLeadLenderSubmittedActivityRequest((LeadLenderSubmissionEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            case LEAD_REJECTED, LEAD_REJECTION_UNDO, LEAD_WITHDRAWN, LEAD_ON_HOLD, LEAD_RESUMED, LEAD_COMPLETED, LEAD_DROPOFF -> {
                CreateLeadActivityRequest request = createLeadStatusChangeActivityRequest(event, (LeadStatusChangeEventPayload) payload, effectiveUsername);
                writeService.createLeadActivity(request);
            }
            default ->{
                //Not supported
            }
        }

    }

    private CreateLeadActivityRequest createLeadCreatedActivityRequest(LeadCreationEventPayload payload, String username){
        return CreateLeadActivityRequest.builder()
                .leadId(payload.getId())
                .resourceId(payload.getId())
                .description("Lead created by " + username)
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.CREATE)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadUpdatedActivityRequest(LeadUpdateEventPayload payload, String username){
        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getLeadId())
                .description("Lead updated by " + username)
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.UPDATE)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadNoteCreatedActivityRequest(LeadNoteCreationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getNoteId())
                .description("Note created by " + username)
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadNoteUpdatedActivityRequest(LeadNoteUpdationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getNoteId())
                .description("Note updated by " + username)
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadNoteDeletedActivityRequest(LeadNoteDeletionEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getNoteId())
                .description("Note deleted by " + username)
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadDocumentCreatedActivityRequest(LeadDocumentCreationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentIdentifier", payload.getDocumentIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getDocumentId())
                .description("Document created by " + username)
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadDocumentUpdatedActivityRequest(LeadDocumentUpdationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentIdentifier", payload.getDocumentIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getDocumentId())
                .description("Document updated by " + username)
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadDocumentDeletedActivityRequest(LeadDocumentDeletionEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentIdentifier", payload.getDocumentIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getDocumentId())
                .description("Document deleted by " + username)
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadContactCreatedActivityRequest(LeadContactCreationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactIdentifier", payload.getContactIdentifier().toString());
        metadata.put("contactType", payload.getContactType());
        metadata.put("isDecisionMaker", payload.getIsDecisionMaker());
        metadata.put("isPropertyOwner", payload.getIsPropertyOwner());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getContactId())
                .description("Contact created by " + username)
                .resource(ResourceEnum.CONTACT)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadContactUpdatedActivityRequest(LeadContactUpdationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactIdentifier", payload.getContactIdentifier().toString());
        metadata.put("contactType", payload.getContactType());
        metadata.put("isDecisionMaker", payload.getIsDecisionMaker());
        metadata.put("isPropertyOwner", payload.getIsPropertyOwner());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getContactId())
                .description("Contact updated by " + username)
                .resource(ResourceEnum.CONTACT)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadContactDeletedActivityRequest(LeadContactDeletionEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactIdentifier", payload.getContactIdentifier().toString());
        metadata.put("contactType", payload.getContactType());
        metadata.put("isDecisionMaker", payload.getIsDecisionMaker());
        metadata.put("isPropertyOwner", payload.getIsPropertyOwner());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getContactId())
                .description("Contact deleted by " + username)
                .resource(ResourceEnum.CONTACT)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadCallLogCreatedActivityRequest(LeadCallLogCreationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("callLogIdentifier", payload.getCallLogIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getCallLogId())
                .description("Call log created by " + username)
                .resource(ResourceEnum.CALL_LOG)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadCallLogUpdatedActivityRequest(LeadCallLogUpdateEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("callLogIdentifier", payload.getCallLogIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getCallLogId())
                .description("Call log updated by " + username)
                .resource(ResourceEnum.CALL_LOG)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadStatusChangeActivityRequest(BusinessEvent event, LeadStatusChangeEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        if (payload.getReason() != null) {
            metadata.put("reason", payload.getReason());
        }

        String description = getStatusChangeDescription(event, username);

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getLeadId())
                .description(description)
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.STATUS_CHANGE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private String getStatusChangeDescription(BusinessEvent event, String username) {
        String action = switch (event) {
            case LEAD_REJECTED -> "rejected";
            case LEAD_REJECTION_UNDO -> "rejection undo";
            case LEAD_WITHDRAWN -> "withdrawn";
            case LEAD_ON_HOLD -> "put on hold";
            case LEAD_RESUMED -> "resumed";
            case LEAD_COMPLETED -> "completed";
            case LEAD_DROPOFF -> "put on dropoff";
            default -> "status changed";
        };
        return "Lead " + action + " by " + username;
    }

    private CreateLeadActivityRequest createLeadLenderCreatedActivityRequest(LeadLenderCreationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lenderIdentifier", payload.getLenderIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getLenderId())
                .description("Lender created by " + username)
                .resource(ResourceEnum.LENDER)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadLenderUpdatedActivityRequest(LeadLenderUpdationEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lenderIdentifier", payload.getLenderIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getLenderId())
                .description("Lender updated by " + username)
                .resource(ResourceEnum.LENDER)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadLenderRejectedActivityRequest(LeadLenderRejectionEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lenderIdentifier", payload.getLenderIdentifier().toString());
        if (payload.getRejectionReason() != null) {
            metadata.put("rejectionReason", payload.getRejectionReason());
        }

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getLenderId())
                .description("Lender rejected by " + username)
                .resource(ResourceEnum.LENDER)
                .action(ResourceAction.STATUS_CHANGE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

    private CreateLeadActivityRequest createLeadLenderSubmittedActivityRequest(LeadLenderSubmissionEventPayload payload, String username){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lenderIdentifier", payload.getLenderIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getLenderId())
                .description("Lender submitted by " + username)
                .resource(ResourceEnum.LENDER)
                .action(ResourceAction.STATUS_CHANGE)
                .metadata(metadata)
                .createdBy(username)
                .build();
    }

}
