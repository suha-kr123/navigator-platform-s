package com.nivasafinance.features.leadactivity.factory;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadContactCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadContactDeletionEventPayload;
import com.nivasafinance.common.events.payload.LeadContactUpdationEventPayload;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadDocumentCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadDocumentDeletionEventPayload;
import com.nivasafinance.common.events.payload.LeadDocumentUpdationEventPayload;
import com.nivasafinance.common.events.payload.LeadNoteCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadNoteDeletionEventPayload;
import com.nivasafinance.common.events.payload.LeadNoteUpdationEventPayload;
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

    public <T> void recordEvent(String eventType, T payload ){

        BusinessEvent event = BusinessEvent.valueOf(eventType);

        switch (event){
            case LEAD_CREATED -> {
                CreateLeadActivityRequest request = createLeadCreatedActivityRequest((LeadCreationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_NOTE_CREATED -> {
                CreateLeadActivityRequest request = createLeadNoteCreatedActivityRequest((LeadNoteCreationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_NOTE_UPDATED -> {
                CreateLeadActivityRequest request = createLeadNoteUpdatedActivityRequest((LeadNoteUpdationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_NOTE_DELETED -> {
                CreateLeadActivityRequest request = createLeadNoteDeletedActivityRequest((LeadNoteDeletionEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_DOCUMENT_CREATED -> {
                CreateLeadActivityRequest request = createLeadDocumentCreatedActivityRequest((LeadDocumentCreationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_DOCUMENT_UPDATED -> {
                CreateLeadActivityRequest request = createLeadDocumentUpdatedActivityRequest((LeadDocumentUpdationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_DOCUMENT_DELETED -> {
                CreateLeadActivityRequest request = createLeadDocumentDeletedActivityRequest((LeadDocumentDeletionEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_CONTACT_CREATED -> {
                CreateLeadActivityRequest request = createLeadContactCreatedActivityRequest((LeadContactCreationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_CONTACT_UPDATED -> {
                CreateLeadActivityRequest request = createLeadContactUpdatedActivityRequest((LeadContactUpdationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_CONTACT_DELETED -> {
                CreateLeadActivityRequest request = createLeadContactDeletedActivityRequest((LeadContactDeletionEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            case LEAD_CALL_LOG_CREATED -> {
                CreateLeadActivityRequest request = createLeadCallLogCreatedActivityRequest((LeadCallLogCreationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            default ->{
                //Not supported
            }
        }

    }

    private CreateLeadActivityRequest createLeadCreatedActivityRequest(LeadCreationEventPayload payload){
        return CreateLeadActivityRequest.builder()
                .leadId(payload.getId())
                .resourceId(payload.getId())
                .description("Lead created by " + UserContext.getUsername())
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.CREATE)
                .build();
    }

    private CreateLeadActivityRequest createLeadNoteCreatedActivityRequest(LeadNoteCreationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getNoteId())
                .description("Note created by " + UserContext.getUsername())
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadNoteUpdatedActivityRequest(LeadNoteUpdationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getNoteId())
                .description("Note updated by " + UserContext.getUsername())
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadNoteDeletedActivityRequest(LeadNoteDeletionEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getNoteId())
                .description("Note deleted by " + UserContext.getUsername())
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadDocumentCreatedActivityRequest(LeadDocumentCreationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentIdentifier", payload.getDocumentIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getDocumentId())
                .description("Document created by " + UserContext.getUsername())
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadDocumentUpdatedActivityRequest(LeadDocumentUpdationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentIdentifier", payload.getDocumentIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getDocumentId())
                .description("Document updated by " + UserContext.getUsername())
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadDocumentDeletedActivityRequest(LeadDocumentDeletionEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentIdentifier", payload.getDocumentIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getDocumentId())
                .description("Document deleted by " + UserContext.getUsername())
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadContactCreatedActivityRequest(LeadContactCreationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactIdentifier", payload.getContactIdentifier().toString());
        metadata.put("contactType", payload.getContactType());
        metadata.put("isDecisionMaker", payload.getIsDecisionMaker());
        metadata.put("isPropertyOwner", payload.getIsPropertyOwner());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getContactId())
                .description("Contact created by " + UserContext.getUsername())
                .resource(ResourceEnum.CONTACT)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadContactUpdatedActivityRequest(LeadContactUpdationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactIdentifier", payload.getContactIdentifier().toString());
        metadata.put("contactType", payload.getContactType());
        metadata.put("isDecisionMaker", payload.getIsDecisionMaker());
        metadata.put("isPropertyOwner", payload.getIsPropertyOwner());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getContactId())
                .description("Contact updated by " + UserContext.getUsername())
                .resource(ResourceEnum.CONTACT)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadContactDeletedActivityRequest(LeadContactDeletionEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactIdentifier", payload.getContactIdentifier().toString());
        metadata.put("contactType", payload.getContactType());
        metadata.put("isDecisionMaker", payload.getIsDecisionMaker());
        metadata.put("isPropertyOwner", payload.getIsPropertyOwner());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getContactId())
                .description("Contact deleted by " + UserContext.getUsername())
                .resource(ResourceEnum.CONTACT)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .build();
    }

    private CreateLeadActivityRequest createLeadCallLogCreatedActivityRequest(LeadCallLogCreationEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("callLogIdentifier", payload.getCallLogIdentifier().toString());

        return CreateLeadActivityRequest.builder()
                .leadId(payload.getLeadId())
                .resourceId(payload.getCallLogId())
                .description("Call log created by " + UserContext.getUsername())
                .resource(ResourceEnum.CALL_LOG)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .build();
    }

}
